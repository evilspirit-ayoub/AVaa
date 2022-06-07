package DSBot.commands;

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
import net.dv8tion.jda.api.entities.Guild;
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
	
	public static void screen(DSBot.Library library, String[] args, Guild guild, Message message, MessageChannel channel, List<Attachment> attachments, String guildName, File toDownload) throws InterruptedException {
		if(args.length > 2 && args[1].equals("remove")) removeScreen(args, guild, message, channel, guildName);
		else addScreen(library, guild, message, channel, attachments, guildName, toDownload);
	}
	
	private static void removeScreen(String[] id, Guild guild, Message message, MessageChannel channel, String guildName) throws InterruptedException {
		Pattern pattern = Pattern.compile("^\\d+$");
		Matcher matcher;
		String find;
		message.addReaction("U+23F2").queue();
		channel.sendTyping().queue();
		List<Message> history = channel.getHistory().retrievePast(100).complete();
		List<String> removedMessages = new ArrayList<>();
		for(int i = 2; i < id.length; i++) {
			matcher = pattern.matcher(id[i]);
			if(matcher.find()) {
				find = matcher.group();
				for(Message context : history)
					if(context.getId().equals(id[i])) {
						List<MessageEmbed> embed = message.getEmbeds();
						if(embed.isEmpty()) {
							removeScreenWithReference(context, guild, channel, guildName);
							removedMessages.add(find);
						} else {
							removeScreenWithContext(embed, guild, guildName);
		                	removedMessages.add(find);
						}
					}
			}
		}
		message.removeReaction("U+23F2").queue();
		EmbedBuilder info = new EmbedBuilder()
				.addField("Removed screen(s) :", removedMessages.stream().collect(Collectors.joining("\n")), false)
				.setColor(0x0000FF)
				.setFooter("Libérez moi svp");
		message.replyEmbeds(info.build()).queue();
	}

	private static void removeScreenWithReference(Message context, Guild guild, MessageChannel channel, String guildName) {
		MessageReference reference = context.getMessageReference();
		if(reference == null) return;
		Message referenceMessage = reference.getMessage();
		File toDownload = new File(guildName + "/" + context.getAuthor().getName());
		referenceMessage.getAttachments()
		.stream().filter(attachment -> attachment.isImage() && !toDownload.exists()).forEach(attachment -> {
			File file = attachment
					.downloadToFile(toDownload)
					.exceptionally(e -> {
						if(toDownload.exists()) toDownload.delete();
			        	e.printStackTrace();
			        	return null;
			        })
					.join();
			try {
				if(file.exists()) {
					BufferedImage downloaded = ImageIO.read(toDownload);
					List<String> ocr = repearResult(channel, OCRUtils.OCR(file, 0, 0, (downloaded.getWidth() / 3) - 1, downloaded.getHeight() - 1, Imgproc.THRESH_BINARY_INV));
					toDownload.delete();
					List<String> winners = getWinners(ocr);
					List<String> loosers = getLoosers(ocr);
					boolean defense = ocr.get(0).equals("DEFENSE") ? true : false;
            		boolean victory = ocr.get(1).equals("VICTOIRE") ? true : false;
            		Ladder.update(guildName, guild, victory ? winners : loosers, defense, false);
				}
			} catch (Exception e1) {
				if(file != null && file.exists()) file.delete();
				e1.printStackTrace();
			}
		});
	}
	
	private static void removeScreenWithContext(List<MessageEmbed> embed, Guild guild, String guildName) {
		List<Field> fields = embed.get(0).getFields();
		List<String> winners = Arrays.asList(fields.get(0).getValue().split("\n"));
		List<String> loosers = Arrays.asList(fields.get(1).getValue().split("\n"));
		boolean defense = embed.get(0).getTitle().contains("Défense");
		boolean victory = embed.get(0).getDescription().contains("Victoire");
    	try { Ladder.update(guildName, guild, victory ? winners : loosers, defense, false); }
    	catch (ClassNotFoundException | IOException | InterruptedException e1) { e1.printStackTrace(); }
	}

	private static void addScreen(Library library, Guild guild, Message message, MessageChannel channel, List<Attachment> attachments, String guildName, File toDownload) {
		if(attachments.isEmpty()) return;
		message.addReaction("U+23F2").queue();
		channel.sendTyping().queue();
		attachments
		.stream().filter(attachment -> attachment.isImage() && !toDownload.exists()).forEach(attachment -> {
			File file = attachment
					.downloadToFile(toDownload)
					.exceptionally(e -> {
						toDownload.delete();
			        	return null;
			        })
					.join();
			try {
				if(file != null && file.exists()) {
					BufferedImage downloaded = ImageIO.read(toDownload);
					List<String> ocr = repearResult(channel, OCRUtils.OCR(file, 0, 0, (downloaded.getWidth() / 3) - 1, downloaded.getHeight() - 1, Imgproc.THRESH_BINARY_INV));
					file.delete();
					EmbedBuilder info = new EmbedBuilder()
							.setTitle("No title.")
							.setDescription("No description.")
							.addField("Gagnants :", "", false)
							.addField("Perdants :", "", false)
							.setColor(0x0000FF)
							.setFooter("Libérez moi svp");
					if(!updatableChannel(channel) || ocr.isEmpty()) {
						message.replyEmbeds(info.build()).queue();
						return;
					}
					List<String> winners = getWinners(ocr);
					List<String> loosers = getLoosers(ocr);
					String fightType = ocr.get(0).equals("ATTAQUE") ? "Attaque" : "Défense";
					String fightResult = ocr.get(1).equals("VICTOIRE") ? "Victoire" : "Défaite";
					String fightContext = ocr.contains("PERCEPTEUR") ? "percepteur" : "prisme";
					String vs = winners.size() + "vs" + loosers.size();
					info
					.clear()
					.setTitle(fightType + " " + fightContext)
					.setDescription(fightResult + " " + vs)
					.addField("Gagnants :", winners.stream().collect(Collectors.joining("\n")), false)
					.addField("Perdants :", loosers.stream().collect(Collectors.joining("\n")), false);
					message.removeReaction("U+23F2").queue();
					message.replyEmbeds(info.build()).setActionRow(
			                Button.of(ButtonStyle.PRIMARY, "example-bot:button:symbols:white_check_mark", "VALIDATE", Emoji.fromUnicode("\u2705")),
			                Button.of(ButtonStyle.PRIMARY, "example-bot:button:symbols:x", "CANCEL", Emoji.fromUnicode("\u274c")))
					.queue(replyMessage -> {
						library.getEventWaiter().waitForEvent(
				                ButtonInteractionEvent.class, 
				                event -> { return checkInteraction(event, replyMessage, message); },
				                event -> {
				                	replyMessage.editMessageComponents().setActionRows().queue();
				                	String selection = event.getComponentId().split(":")[3];
				                	if(selection.equals("white_check_mark")) {
				                		info.setColor(0x00FF00);
				                		boolean defense = ocr.get(0).equals("DEFENSE") ? true : false;
				                		boolean victory = ocr.get(1).equals("VICTOIRE") ? true : false;
					                	try { Ladder.update(guildName, guild, victory ? winners : loosers, defense, true); }
					                	catch (ClassNotFoundException | IOException | InterruptedException e1) { e1.printStackTrace(); }
					                	info.setFooter(replyMessage.getId());
				                	} else info.setColor(0xFF0000);
				                	replyMessage.editMessageEmbeds(info.build()).queue();
				                },
				                30,
				                TimeUnit.SECONDS,
				                () -> {
				                	message.removeReaction("U+23F2").queue();
				                	replyMessage.editMessageEmbeds(info.clear().setTitle("Expiration.").setColor(0xFF0000).build()).setActionRows().queue();
				                }
				            );
					});
				}
			} catch (Exception e1) { e1.printStackTrace(); } finally { message.removeReaction("U+23F2").queue(); file.delete(); }
		});
	}

	private static List<String> repearResult(MessageChannel channel, List<String> ocr) throws TesseractException, Exception {
		ocr = filter(ocr);
		if(!ocr.contains("GAGNANTS") || !ocr.contains("PERDANTS")) return new ArrayList<>();
		if(!ocr.contains("PERCEPTEUR") && !ocr.contains("PRISME")) return new ArrayList<>();
		if(isBadNumberOfPlayers(ocr)) return new ArrayList<>();
		List<String> newRes = new ArrayList<>();
		if(channel.getName().toLowerCase().contains("attaque")) {
			newRes.add("ATTAQUE");
			if(isWinningAttack(ocr)) newRes.add("VICTOIRE");
			else newRes.add("DEFAITE");
		} else if(channel.getName().toLowerCase().contains("défense")) {
			newRes.add("DEFENSE");
			if(isWinningDefense(ocr)) newRes.add("VICTOIRE");
			else newRes.add("DEFAITE");
		}
		newRes.addAll(capitalizePseudos(ocr));
		return newRes;
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

	private static boolean isWinningDefense(List<String> ocr) {
		for(int i = 0; i < ocr.size(); i++) {
			if(ocr.get(i).equals("GAGNANTS")) {
				for(int j = i + 1; j < ocr.size(); j++) {
					if(ocr.get(j).equals("PERDANTS")) return false;
					if(ocr.get(j).equals("PERCEPTEUR") || ocr.get(j).equals("PRISME")) return true;
				}
			}
		}
		return false;
	}
	
	private static boolean isWinningAttack(List<String> ocr) { return !isWinningDefense(ocr); }

	private static List<String> capitalizePseudos(List<String> ocr) {
		for(int i = 0; i < ocr.size(); i++) ocr.set(i, ocr.get(i).substring(0, 1).toUpperCase() + ocr.get(i).substring(1));
		return ocr;
	}
	
	private static List<String> filter(List<String> ocr) {
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
		int index = 0;
		while(filtered.size() != 0) {
			if(filtered.get(index).equals("VICTOIRE") || filtered.get(index).equals("DEFAITE")) { index++; continue; }
    		if(filtered.get(index).equals("GAGNANTS")) break;
    		filtered.remove(index);
    	}
    	return filtered;
	}
	
	private static boolean updatableChannel(MessageChannel channel) {
		return channel.getName().toLowerCase().contains("attaque") || channel.getName().toLowerCase().contains("défense");
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
    
    private static List<String> getWinners(List<String> ocr) {
    	List<String> res = new ArrayList<>();
    	for(int i = 0; i < ocr.size(); i++) {
    		if(ocr.get(i).equals("PERDANTS")) break;
    		else if(ocr.get(i).equals("DEFENSE")
    				|| ocr.get(i).equals("GAGNANTS")
    				|| ocr.get(i).equals("ATTAQUE")
    				|| ocr.get(i).equals("DEFAITE")
    				|| ocr.get(i).equals("VICTOIRE")
    				|| ocr.get(i).equals("PERCEPTEUR")
    				|| ocr.get(i).equals("PRISME")) continue;
    		else res.add(ocr.get(i));
    	}
    	return res;
    }
    
    private static List<String> getLoosers(List<String> ocr) {
    	List<String> res = new ArrayList<>();
    	for(int i = 0; i < ocr.size(); i++)
    		if(ocr.get(i).equals("PERDANTS"))
    			for(int j = i + 1; j < ocr.size(); j++)
    				if(ocr.get(j).equals("PERCEPTEUR") || ocr.get(j).equals("PRISME")) continue;
    				else res.add(ocr.get(j));
    	return res;
    }
}
