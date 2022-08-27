package DSBot.command;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.opencv.imgproc.Imgproc;

import DSBot.Library;
import DSBot.database.model.Ladder;
import DSBot.database.model.Screen;
import DSBot.exception.DSBotException;
import DSBot.utils.ocr.OCRUtils;
import DSBot.utils.string.JaroWinklerStrategy;
import DSBot.utils.string.SimilarityStrategy;
import DSBot.utils.string.StringSimilarityService;
import DSBot.utils.string.StringSimilarityServiceImpl;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.sourceforge.tess4j.TesseractException;

public class CommandScreen implements CommandExecutor {
	
	public static final String SCALE_FILE = "bareme.txt";
	private static Message message;
	private static MessageChannel channel;
	private static List<Attachment> attachments;
	private static Library library;
	private static String[] args;
	
	@Override
	public void run(MessageReceivedEvent event, Command command, Library library, String[] args) throws Exception {
		message = event.getMessage();
		channel = message.getChannel();
		attachments = message.getAttachments();
		CommandScreen.library = library;
		CommandScreen.args = args;
		screen();
	}

	private void screen() throws Exception {
		if(!channel.getName().contains("screen-defense"))
			throw new DSBotException(message, "Mauvais channel.");
		if(args.length > 1 && args[1].equals("remove")) removeScreen();
		else addScreen();
	}
	
	private static void removeScreen() throws Exception {
		channel.sendTyping().queue();
		message.addReaction("U+23F2").queue();
		if(!authorizedToRemoveScreen()) {
			message.removeReaction("U+23F2").queue();
			throw new DSBotException(message, "Non autorise pour la plebe.");
		}
		EmbedBuilder info = new EmbedBuilder();
		info.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl());
		if(args.length < 3) {
			message.removeReaction("U+23F2").queue();
			throw new DSBotException(message, "La commande doit contenir deux arguments.");
		}
		Pattern pattern = Pattern.compile("^\\d+$");
		Matcher matcher;
		String find;
		matcher = pattern.matcher(args[2]);
		if(matcher.find()) {
			find = matcher.group();
			Screen screen = Screen.getScreen(find);
			if(screen == null)
				throw new DSBotException(message, "Le screen ayant l id " + find + " n existe pas.");
			Screen.delete(find);
			Ladder.updatePoints(message.getGuild(), screen.getPseudos(), (-1) * screen.getPoints());
			Ladder.updatePoisitonsForLinkedUsers();
			Ladder.updateThisMonthLadder();
			Ladder.refreshDiscordChannelLadder(message.getGuild());
			info.setTitle("Screen enleve.");
			message.replyEmbeds(info.build()).queue();
			message.removeReaction("U+23F2").queue();
		} else throw new DSBotException(message, "Le screen ayant l id " + args[2] + " n existe pas.");
	}
	
	private static boolean authorizedToRemoveScreen() {
		Member messageSender = message.getMember();
		if(messageSender.hasPermission(Permission.VIEW_AUDIT_LOGS)) return true;
		if(messageSender.hasPermission(Permission.KICK_MEMBERS)) return true;
		if(messageSender.hasPermission(Permission.BAN_MEMBERS)) return true;
		return false;
	}
	
	public static File downloadAttachment(Attachment attachment, File file) {
		return attachment
				.downloadToFile(file)
				.exceptionally(error -> {
					if(file.exists()) file.delete();
		        	error.printStackTrace();
		        	return null;
		        })
				.join();
	}
	
	private static List<String> filterOcrResult(List<String> ocr) {
		Pattern pattern = Pattern.compile("[A-Z‚ÍËÈÓÁÙ˚ÔÊ¬ Œ‘€ƒÀœ÷‹¿∆«…»åúŸ][a-z‚ÍËÈÓÁÙ˚ÔÊ¬ Œ‘€ƒÀœ÷‹¿∆«…»åúŸ]*(-[a-zA-Z‚ÍËÈÓÁÙ˚ÔÊ¬ Œ‘€ƒÀœ÷‹¿∆«…»åúŸ][a-z‚ÍËÈÓÁÙ˚ÔÊ¬ Œ‘€ƒÀœ÷‹¿∆«…»åúŸ]*)*");
		Matcher matcher;
		Pattern patternPerco = Pattern.compile("Do'|do'| Do | do |Do[A-Z]|Doë|doë");
		String str, keep, find;
		List<String> filtered = new ArrayList<>();
		for(int i = 0; i < ocr.size(); i++) {
			str = ocr.get(i);
			if(str.contains("Prisme") || str.contains("prisme") || str.contains("Prism") || str.contains("prism") || str.contains("alliance")) {
				filtered.add("PRISME");
				continue;
			}
			matcher = patternPerco.matcher(str);
			if(matcher.find()) {
				filtered.add("PERCEPTEUR");
				continue;
			}
			if(str.contains("Gagnants") || str.contains("agnants") || str.contains("agnant")) {
				filtered.add("GAGNANTS");
				continue;
			}
			if(str.contains("Perdants") || str.contains("erdants") || str.contains("erdant")) {
				filtered.add("PERDANTS");
				continue;
			}
			matcher = pattern.matcher(str);
			keep = "";
			while(matcher.find()) {
				find = matcher.group();
				if(find.length() > 3 && find.length() > keep.length()) keep = find;
			}
			if(!keep.isEmpty()) filtered.add(keep);
		}
		while(filtered.size() != 0) {
    		if(filtered.get(0).equals("GAGNANTS")) break;
    		filtered.remove(0);
    	}
		return filterRepeatedPattern(filtered);
	}
	
	private static List<String> filterRepeatedPattern(List<String> filtered) {
		List<String> repeated = new ArrayList<>();
		SimilarityStrategy strategy = new JaroWinklerStrategy();
		StringSimilarityService service = new StringSimilarityServiceImpl(strategy);
		for(int i = 0; i < filtered.size(); i++) {
			if(filtered.get(i).equals("GAGNANTS") || filtered.get(i).equals("PERDANTS") || filtered.get(i).equals("PERCEPTEUR") || filtered.get(i).equals("PRISME")) continue;
			for(int j = i + 1; j < filtered.size(); j++) {
				if(filtered.get(j).equals("GAGNANTS") || filtered.get(j).equals("PERDANTS") || filtered.get(j).equals("PERCEPTEUR") || filtered.get(j).equals("PRISME")) continue;
				if(service.score(filtered.get(i), filtered.get(j)) >= 0.91)
					repeated.add(filtered.get(i).length() < filtered.get(j).length() ? filtered.get(i) : filtered.get(j));
			}
		}
		for(String pseudo : repeated) filtered.remove(pseudo);
		return filtered;
	}

	private static boolean checkOcrResult(List<String> ocr) throws TesseractException, Exception {
		if(!ocr.contains("GAGNANTS") || !ocr.contains("PERDANTS")) return false;
		if(!ocr.contains("PERCEPTEUR") && !ocr.contains("PRISME")) return false;
		if(isBadNumberOfPlayers(ocr)) return false;
		return true;
	}
	
	private static boolean isBadNumberOfPlayers(List<String> ocr) {
		int numberWinningPlayer = 0;
		int numberLoosingPlayer = 0;
		for(int i = 0; i < ocr.size(); i++) {
			if(ocr.get(i).equals("GAGNANTS")) {
				for(int j = i + 1; j < ocr.size(); j++) {
					if(ocr.get(j).equals("PERCEPTEUR") || ocr.get(j).equals("PRISME")) continue;
					if(ocr.get(j).equals("PERDANTS")) {
						for(int k = j + 1; k < ocr.size(); k++) {
							if(ocr.get(k).equals("PERCEPTEUR") || ocr.get(k).equals("PRISME")) continue;
							numberLoosingPlayer++;
						}
						return numberWinningPlayer == 0 || numberWinningPlayer > 5 || numberLoosingPlayer > 5;
					}
					numberWinningPlayer ++;
				}
			}
		}
		return true;
	}
	
	private static List<String> capitalizePseudos(List<String> ocr) {
		for(int i = 0; i < ocr.size(); i++)
			ocr.set(i, ocr.get(i).substring(0, 1).toUpperCase() + ocr.get(i).substring(1));
		return ocr;
	}
	
	private static List<String> getWinners(List<String> ocr) {
    	List<String> res = new ArrayList<>();
    	for(int i = 1; i < ocr.size(); i++) {
    		if(ocr.get(i).equals("PERDANTS")) break;
    		if(ocr.get(i).equals("PERCEPTEUR") || ocr.get(i).equals("PRISME")) continue;
    		res.add(ocr.get(i));
    	}
    	return res;
    }
    
    private static List<String> getLoosers(List<String> ocr) {
    	List<String> res = new ArrayList<>();
    	for(int i = 1; i < ocr.size(); i++)
    		if(ocr.get(i).equals("PERDANTS"))
    			for(int j = i + 1; j < ocr.size(); j++)
    				if(ocr.get(j).equals("PERCEPTEUR") || ocr.get(j).equals("PRISME")) continue;
    				else res.add(ocr.get(j));
    	return res;
    }

    private static void addScreen() throws Exception {
    	channel.sendTyping().queue();
    	message.addReaction("U+23F2").queue();
    	if(attachments.isEmpty()) {
    		message.removeReaction("U+23F2").queue();
    		throw new DSBotException(message, "Aucune image detecte.");
    	}
    	Attachment attachment = attachments.get(0);
		if(!attachment.isImage()) {
			message.removeReaction("U+23F2").queue();
			throw new DSBotException(message, "L attachment n est pas une image.");
		}
		String authorName = message.getAuthor().getName();
		File toDownload = new File(authorName);
		if(toDownload.exists()) toDownload.delete();
		toDownload = downloadAttachment(attachment, toDownload);
		if(toDownload == null) {
			message.removeReaction("U+23F2").queue();
			throw new DSBotException(message, "Erreur pendant le telechargement.");
		}
		BufferedImage attachmentBuffer = ImageIO.read(toDownload);
		List<String> ocr = filterOcrResult(OCRUtils.OCR(toDownload, 0, 0, (attachmentBuffer.getWidth() / 4) - 1, attachmentBuffer.getHeight() - 1, Imgproc.THRESH_BINARY_INV));
		for(String s : ocr) System.out.println(s);
		System.out.println("/////////////");
		if(!checkOcrResult(ocr)) {
			message.removeReaction("U+23F2").queue();
			toDownload.delete();
			throw new DSBotException(message, "Reessayez avec un autre screen mieux cadre et/ou avec un autre theme et/ou plus grand.");
		}
		capitalizePseudos(ocr);
		toDownload.delete();
		List<String> winners = getWinners(ocr);
		List<String> loosers = getLoosers(ocr);
		int indexOfFightContext = ocr.contains("PERCEPTEUR") ? ocr.indexOf("PERCEPTEUR") : ocr.indexOf("PRISME");
		int indexOfLoosers = ocr.indexOf("PERDANTS");
		boolean isVictory = indexOfFightContext < indexOfLoosers;
		String versus = isVictory ? winners.size() + "vs" + loosers.size() : loosers.size() + "vs" + winners.size();
		EmbedBuilder info = new EmbedBuilder()
				.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl())
				.setTitle(Emoji.fromUnicode("\u2694").getAsMention() + " Defense " + (ocr.contains("PERCEPTEUR") ? "percepteur" : "prisme"))
				.setDescription((isVictory ? "Victoire " : "Defaite ") + versus)
				.addField("Gagnants :", winners.stream().map(pseudo -> pseudo = "- " + pseudo).collect(Collectors.joining("\n")), true)
				.addField("Perdants :", loosers.stream().map(pseudo -> pseudo = "- " + pseudo).collect(Collectors.joining("\n")), true);
		message.replyEmbeds(info.build()).setActionRow(
                Button.of(ButtonStyle.PRIMARY, "example-bot:button:symbols:white_check_mark", "VALIDATE", Emoji.fromUnicode("\u2705")),
                Button.of(ButtonStyle.PRIMARY, "example-bot:button:symbols:x", "CANCEL", Emoji.fromUnicode("\u274c")))
		.queue(replyMessage -> {
			library
			.getEventWaiter()
			.waitForEvent(
					ButtonInteractionEvent.class, 
	                event -> { return checkInteraction(event, replyMessage); },
	                event -> {
	                	replyMessage.editMessageComponents().setActionRows().queue();
	                	String selection = event.getComponentId().split(":")[3];
	                	if(selection.equals("white_check_mark")) {
	                		try {
	                			info.setColor(0x00FF00);
	                			float points = getPointsAccordingToScale(versus, isVictory);
	                			info.addField("Points :", String.valueOf(points), false);
	                			Screen screen = new Screen(replyMessage.getId(), isVictory ? winners : loosers, isVictory, versus, points, LocalDate.now());
	                			screen.insert();
	                			Ladder.updatePoints(message.getGuild(), isVictory ? winners : loosers, points);
	                			Ladder.updatePoisitonsForLinkedUsers();
	                			Ladder.updateThisMonthLadder();
	                			Ladder.refreshDiscordChannelLadder(message.getGuild());
	                			info.setFooter(replyMessage.getId());
	                		}catch (SQLException | IOException e) { e.printStackTrace(); }
	                	} else info.setColor(0xFF0000);
	                	replyMessage.editMessageEmbeds(info.build()).queue();
	                },
	                45,
	                TimeUnit.SECONDS,
	                () -> {
	                	replyMessage.editMessageEmbeds(info.clear().setTitle("Expiration.").setColor(0xFF0000).build()).setActionRows().queue();
	                });
		});
		message.removeReaction("U+23F2").queue();
    }
    
    private static boolean checkInteraction(ButtonInteractionEvent event, Message replyMessage) {
		if (event.getUser().getIdLong() != message.getAuthor().getIdLong()) return false;
        if (event.getMessageIdLong() != replyMessage.getIdLong()) return false;
        if (!equalsAny(event.getComponentId())) return false;
        return !event.isAcknowledged();
	}
	
    private static boolean equalsAny(String buttonId) {
        return buttonId.equals("example-bot:button:symbols:white_check_mark") ||
               buttonId.equals("example-bot:button:symbols:x");
    }
    
    private static float getPointsAccordingToScale(String versus, boolean isVictory) throws FileNotFoundException, IOException {
	    try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(SCALE_FILE)))) {
	    	String line;
	        while ((line = br.readLine()) != null)
	        	if(line.contains(versus))
	        		return isVictory ? Float.parseFloat(line.split(" ")[1].replaceAll("\n", "")) : Float.parseFloat(line.split(" ")[2].replaceAll("\n", ""));
	    }
	    return 0;
    }
}
