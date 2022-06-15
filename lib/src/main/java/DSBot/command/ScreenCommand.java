package DSBot.command;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
import DSBot.utils.OCRUtils;
import Ladder.Ladder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageEmbed.Field;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.sourceforge.tess4j.TesseractException;

public class ScreenCommand {
	
	public static void screen(Library library, String[] args, Message message) throws Exception {
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
		return channel.getName().contains("screen-attaque") || channel.getName().contains("screen-défense");
	}
	
	private static void removeScreen(String[] args, Message message) throws Exception {
		if(args.length < 3) throw new Exception("Invalid number of arguments.");
		message.addReaction("U+23F2").queue();
		Pattern pattern = Pattern.compile("^\\d+$");
		Matcher matcher;
		String find;
		List<Message> history = message.getChannel().getHistory().retrievePast(100).complete();
		List<String> removedMessages = new ArrayList<>();
		for(int i = 2; i < args.length; i++) {
			matcher = pattern.matcher(args[i]);
			if(matcher.find()) {
				find = matcher.group();
				for(Message msg : history)
					if(msg.getId().equals(args[i])) {
						List<MessageEmbed> embed = message.getEmbeds();
						if(embed.isEmpty()) removeScreenWithReferenceMessage(msg);
						else removeScreenWithOriginMessage(msg);
						removedMessages.add(find);
					}
			}
		}
		EmbedBuilder info = new EmbedBuilder()
				.addField("Removed screen(s) :", removedMessages.stream().collect(Collectors.joining("\n")), false)
				.setColor(0x0000FF);
		message.replyEmbeds(info.build()).queue();
		message.removeReaction("U+23F2").queue();
	}
	
	private static void removeScreenWithReferenceMessage(Message origin) throws Exception {
		MessageReference reference = origin.getMessageReference();
		if(reference == null) throw new Exception("Message has no reference");
		Message context = reference.getMessage();
		MessageChannel channel = origin.getChannel();
		String guildName = context.getGuild().getName(), authorName = context.getAuthor().getName();
		File toDownload = new File(guildName + "/" + authorName);
		context
		.getAttachments()
		.stream()
		.filter(attachment -> attachment.isImage() && !toDownload.exists()).forEach(attachment -> {
			File file = downloadAttachment(attachment, toDownload);
			if(file == null) return;
			try {
				BufferedImage downloaded = ImageIO.read(toDownload);
				List<String> ocr = filterOcrResult(OCRUtils.OCR(file, 0, 0, (downloaded.getWidth() / 3) - 1, downloaded.getHeight() - 1, Imgproc.THRESH_BINARY_INV));
				checkOcrResult(ocr);
				capitalizePseudos(ocr);
				toDownload.delete();
				boolean defense = channel.getName().contains("défense") ? true : false;
				Ladder.update(origin.getGuild(), collectPlayersWhoDeservePoints(channel, ocr), defense, -1);
			} catch(Exception e) { e.printStackTrace(); } finally { origin.removeReaction("U+23F2").queue(); file.delete(); }
		});
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
    	return filtered;
	}
	
	private static void checkOcrResult(List<String> ocr) throws TesseractException, Exception {
		if(!ocr.contains("GAGNANTS") || !ocr.contains("PERDANTS")) throw new Exception("Wrong ocr result.");
		if(!ocr.contains("PERCEPTEUR") && !ocr.contains("PRISME")) throw new Exception("Wrong ocr result.");
		if(isBadNumberOfPlayers(ocr)) throw new Exception("Wrong ocr result.");
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
	
	private static List<String> collectPlayersWhoDeservePoints(MessageChannel channel, List<String> ocr) {
		boolean attack = channel.getName().contains("attaque") ? true : false;
		int indexLoosers = ocr.indexOf("PERDANTS");
		int indexFightContext = ocr.contains("PERCEPTEUR") ? ocr.indexOf("PERCEPTEUR") : ocr.indexOf("PRISME");
		boolean victory = indexFightContext < indexLoosers ? (attack ? false : true) : (attack ? true : false);
		if(victory) return getWinners(ocr);
		return getLoosers(ocr);
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
    
    private static void removeScreenWithOriginMessage(Message message) {
    	MessageEmbed embed = message.getEmbeds().get(0);
		List<Field> fields = embed.getFields();
		List<String> winners = Arrays.asList(fields.get(0).getValue().split("\n"));
		List<String> loosers = Arrays.asList(fields.get(1).getValue().split("\n"));
		boolean defense = embed.getTitle().contains("Défense");
		boolean victory = embed.getDescription().contains("Victoire");
    	try { Ladder.update(message.getGuild(), victory ? winners : loosers, defense, -1); }
    	catch (ClassNotFoundException | IOException | InterruptedException e1) { e1.printStackTrace(); }
	}
    
    private static void addScreen(Library library, String[] args, Message message) throws Exception {
    	List<Attachment> attachments = message.getAttachments()	;
    	if(attachments.isEmpty()) throw new Exception("No attachments.");
    	message.addReaction("U+23F2").queue();
    	String guildName = message.getGuild().getName(), authorName = message.getAuthor().getName();
		File toDownload = new File(guildName + "/" + authorName);
    	attachments.stream().filter(attachment -> attachment.isImage() && !toDownload.exists()).forEach(attachment -> {
			File file = downloadAttachment(attachment, toDownload);
			if(file == null) return;
			try {
				BufferedImage downloaded = ImageIO.read(toDownload);
				List<String> ocr = filterOcrResult(OCRUtils.OCR(file, 0, 0, (downloaded.getWidth() / 3) - 1, downloaded.getHeight() - 1, Imgproc.THRESH_BINARY_INV));
				checkOcrResult(ocr);
				capitalizePseudos(ocr);
				toDownload.delete();
				boolean attack = message.getChannel().getName().contains("attaque") ? true : false;
				int indexLoosers = ocr.indexOf("PERDANTS");
				int indexFightContext = ocr.contains("PERCEPTEUR") ? ocr.indexOf("PERCEPTEUR") : ocr.indexOf("PRISME");
				boolean victory = indexFightContext < indexLoosers ? (attack ? false : true) : (attack ? true : false);
				List<String> winners = getWinners(ocr);
				List<String> loosers = getLoosers(ocr);
				EmbedBuilder info = new EmbedBuilder()
				.setTitle((attack ? "Attaque " : "Défense ") + (ocr.contains("PERCEPTEUR") ? "percepteur" : "prisme"))
				.setDescription((victory ? "Victoire " : "Défaite ") + winners.size() + "vs" + loosers.size())
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
				                	try { Ladder.update(message.getGuild(), victory ? winners : loosers, attack ? false : true, 1); }
				                	catch (ClassNotFoundException | IOException | InterruptedException e1) { e1.printStackTrace(); }
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
}
