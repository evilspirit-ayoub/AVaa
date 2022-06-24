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
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.sourceforge.tess4j.TesseractException;

public class CommandScreen implements CommandExecutor {
	
	private static final String SCALE_FILE = "bareme.txt";

	@Override
	public void run(MessageReceivedEvent event, Command command, Library library, String[] args) throws Exception {
		Message message = event.getMessage();
		MessageChannel channel = message.getChannel();
		channel.sendTyping().queue();
		if(!isGoodChannel(channel)) {
			message.replyEmbeds(new EmbedBuilder().setTitle("Mauvais channel.").build()).queue();
			return;
		}
		if(args.length > 1 && args[1].equals("remove")) {
			removeScreen(args, message);
			return;
		} else addScreen(library, args, message);
	}

	private static boolean isGoodChannel(MessageChannel channel) {
		return channel.getName().contains("screen-defense");
	}
	
	private static void removeScreen(String[] args, Message message) throws Exception {
		EmbedBuilder info = new EmbedBuilder();
		info.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl());
		message.addReaction("U+23F2").queue();
		if(args.length < 3) info.setTitle("Invalid number of arguments.");
		else if(authorizedToRemoveScreen(message.getMember())) {
			Pattern pattern = Pattern.compile("^\\d+$");
			Matcher matcher;
			String find;
			List<Message> history = message.getChannel().getHistory().retrievePast(100).complete();
			List<String> removedMessages = new ArrayList<>();
			List<String> notRemovedMessages = new ArrayList<>();
			for(int i = 2; i < args.length; i++) {
				matcher = pattern.matcher(args[i]);
				if(matcher.find()) {
					find = matcher.group();
					for(Message msg : history)
						if(msg.getId().equals(args[i])) {
							MessageReference reference = msg.getMessageReference();
							if(reference == null) {
								notRemovedMessages.add(args[i]);
								break;
							}
							Message context = reference.getMessage();
							String authorName = context.getAuthor().getName();
							File toDownload = new File(authorName);
							context
							.getAttachments()
							.stream()
							.filter(attachment -> attachment.isImage() && !toDownload.exists()).forEach(attachment -> {
								File file = downloadAttachment(attachment, toDownload);
								if(file == null) return;
								try {
									BufferedImage downloaded = ImageIO.read(toDownload);
									List<String> ocr = filterOcrResult(OCRUtils.OCR(file, 0, 0, (downloaded.getWidth() / 3) - 1, downloaded.getHeight() - 1, Imgproc.THRESH_BINARY_INV));
									capitalizePseudos(ocr);
									toDownload.delete();
									List<String> winners = getWinners(ocr);
									List<String> loosers = getLoosers(ocr);
									int indexOfFightContext = ocr.contains("PERCEPTEUR") ? ocr.indexOf("PERCEPTEUR") : ocr.indexOf("PRISME");
									int indexOfLoosers = ocr.indexOf("PERDANTS");
									boolean isVictory = indexOfFightContext < indexOfLoosers;
									String versus = isVictory ? winners.size() + "vs" + loosers.size() : loosers.size() + "vs" + winners.size();
									Ladder.update(context.getGuild(), isVictory ? winners : loosers, -1 * getPointsAccordingToScale(versus, isVictory));
								} catch(Exception e) { e.printStackTrace(); } finally { file.delete(); }
							});
							Screen.delete(args[i]);
							removedMessages.add(find);
							break;
						}
				}
			}
			info.addField("Screen(s) enleve(s) :", removedMessages.stream().collect(Collectors.joining("\n")), false);
			if(!notRemovedMessages.isEmpty()) info.addField("Screen(s) non enleve(s) :", notRemovedMessages.stream().collect(Collectors.joining("\n")), false);
		} else info.setTitle("Non autorise.");
		message.replyEmbeds(info.build()).queue();
		message.removeReaction("U+23F2").queue();
	}
	
	private static boolean authorizedToRemoveScreen(Member messageSenderMember) {
		if(messageSenderMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) return true;
		if(messageSenderMember.hasPermission(Permission.KICK_MEMBERS)) return true;
		if(messageSenderMember.hasPermission(Permission.BAN_MEMBERS)) return true;
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
		Pattern pattern = Pattern.compile("^[a-zA-ZâêèéîçôoûïæÂÊÎÔÛÄËÏÖÜÀÆÇÉÈŒœÙ][a-zâêèéîçôoûïæÂÊÎÔÛÄËÏÖÜÀÆÇÉÈŒœÙ]+([-][a-zA-ZâêèéîçôoûïæÂÊÎÔÛÄËÏÖÜÀÆÇÉÈŒœÙ][a-zâêèéîçôoûïæÂÊÎÔÛÄËÏÖÜÀÆÇÉÈŒœÙ]*)*$|[A-Za-zâêèéîçôoûïæÂÊÎÔÛÄËÏÖÜÀÆÇÉÈŒœÙ][a-zâêèéîçôoûïÂÊÎÔÛÄËÏÖÜÀÆÇÉÈŒœÙ]+([-][a-zA-ZâêèéîçôoûïæÂÊÎÔÛÄËÏÖÜÀÆÇÉÈŒœÙ][a-zâêèéîçôoûïæÂÊÎÔÛÄËÏÖÜÀÆÇÉÈŒœÙ]*)*");
		//Pattern pattern = Pattern.compile("^[a-zA-Z][a-z]+([-][a-zA-Z][a-z]*)*$|[A-Za-z][a-z]+([-][a-zA-Z][a-z]*)*");
		Matcher matcher;
		String str, keep, find;
		List<String> filtered = new ArrayList<>();
		for(int i = 0; i < ocr.size(); i++) {
			str = ocr.get(i);
			if(str.contains("Prism") || str.contains("prism") || str.contains("alliance")) filtered.add("PRISME");
			else if(str.contains("Do") || str.contains("do'")) filtered.add("PERCEPTEUR");
			else if(str.contains("Gagnants") || str.contains("agnants") || str.contains("agnant")) filtered.add("GAGNANTS");
			else if(str.contains("Perdants") || str.contains("erdants") || str.contains("erdant")) filtered.add("PERDANTS");
			else {
				matcher = pattern.matcher(str);
				keep = "";
				while(matcher.find()) {
					find = matcher.group();
					if(find.length() > 2 && find.length() > keep.length()) keep = find;
				}
				if(!keep.isEmpty()) filtered.add(keep);
			}
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
				if(service.score(filtered.get(i), filtered.get(j)) >= 0.8)
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
		for(int i = 0; i < ocr.size(); i++) ocr.set(i, ocr.get(i).substring(0, 1).toUpperCase() + ocr.get(i).substring(1));
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
    
    private static void addScreen(Library library, String[] args, Message message) throws Exception {
    	List<Attachment> attachments = message.getAttachments();
    	EmbedBuilder info = new EmbedBuilder();
    	if(attachments.isEmpty()) {
    		message.replyEmbeds(info.setTitle("No attachments.").build()).queue();
    		return;
    	}
    	message.addReaction("U+23F2").queue();
    	String authorName = message.getAuthor().getName();
		File toDownload = new File(authorName);
    	attachments.stream().filter(attachment -> attachment.isImage() && !toDownload.exists()).forEach(attachment -> {
			File file = downloadAttachment(attachment, toDownload);
			if(file == null) return;
			try {
				BufferedImage downloaded = ImageIO.read(toDownload);
				List<String> ocr = filterOcrResult(OCRUtils.OCR(file, 0, 0, (downloaded.getWidth() / 3) - 1, downloaded.getHeight() - 1, Imgproc.THRESH_BINARY_INV));
				if(!checkOcrResult(ocr)) {
					message.replyEmbeds(info.setTitle("Reessayez avec un autre screen mieux cradre et/ou avec un autre theme.").build()).queue();
					return;
				}
				capitalizePseudos(ocr);
				toDownload.delete();
				List<String> winners = getWinners(ocr);
				List<String> loosers = getLoosers(ocr);
				int indexOfFightContext = ocr.contains("PERCEPTEUR") ? ocr.indexOf("PERCEPTEUR") : ocr.indexOf("PRISME");
				int indexOfLoosers = ocr.indexOf("PERDANTS");
				boolean isVictory = indexOfFightContext < indexOfLoosers;
				String versus = isVictory ? winners.size() + "vs" + loosers.size() : loosers.size() + "vs" + winners.size();
				info.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl())
				.setTitle("Defense " + (ocr.contains("PERCEPTEUR") ? "percepteur" : "prisme"))
				.setDescription((isVictory ? "Victoire " : "Defaite ") + versus)
				.addField("Gagnants :", winners.stream().collect(Collectors.joining("\n")), false)
				.addField("Perdants :", loosers.stream().collect(Collectors.joining("\n")), false);
				message.replyEmbeds(info.build()).setActionRow(
		                Button.of(ButtonStyle.PRIMARY, "example-bot:button:symbols:white_check_mark", "VALIDATE", Emoji.fromUnicode("\u2705")),
		                Button.of(ButtonStyle.PRIMARY, "example-bot:button:symbols:x", "CANCEL", Emoji.fromUnicode("\u274c")))
				.queue(replyMessage -> {
					library
					.getEventWaiter()
					.waitForEvent(ButtonInteractionEvent.class, 
			                event -> { return checkInteraction(event, replyMessage, message); },
			                event -> {
			                	replyMessage.editMessageComponents().setActionRows().queue();
			                	String selection = event.getComponentId().split(":")[3];
			                	if(selection.equals("white_check_mark")) {
			                		info.setColor(0x00FF00);
			                		try {
			                			float points = getPointsAccordingToScale(versus, isVictory);
			                			info.addField("Points : ", String.valueOf(points), false);
			                			Screen screen = new Screen(replyMessage.getId(), isVictory ? winners : loosers, isVictory, versus, points, LocalDate.now());
			                			screen.insert();
			                			Ladder.update(message.getGuild(), isVictory ? winners : loosers, points);
			                		}catch (SQLException | IOException e) { e.printStackTrace(); }
				                	info.setFooter(replyMessage.getId());
			                	} else info.setColor(0xFF0000);
			                	replyMessage.editMessageEmbeds(info.build()).queue();
			                },
			                30,
			                TimeUnit.SECONDS,
			                () -> {
			                	replyMessage.editMessageEmbeds(info.clear().setTitle("Expiration.").setColor(0xFF0000).build()).setActionRows().queue();
			                });
				});
				message.removeReaction("U+23F2").queue();
			} catch (Exception e1) { e1.printStackTrace(); } finally { message.removeReaction("U+23F2").queue(); file.delete(); }
		});
    }
    
    private static boolean checkInteraction(ButtonInteractionEvent event, Message replyMessage, Message message) {
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
