package DSBot.command;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.opencv.imgproc.Imgproc;

import DSBot.utils.OCRUtils;
import Ladder.Ladder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageReference;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message.Attachment;

public class ScreenCommand {
	
	/*public static void screen(String[] args, Message message) throws InterruptedException {
		Guild guild = message.getGuild();
		String guildName = guild.getName();
		MessageChannel channel = message.getChannel();
		if(!isGoodChannel(channel)) {
			message.replyEmbeds(new EmbedBuilder().setTitle("Mauvais channel.").build()).queue();
			return;
		}
		if(isRemoveScreenCommand(args)) {
			removeScreen(args, message);
			return;
		}
		if(isAddScreenCommand(args)) {
			addScreen(args, message);
		}
		if(args.length > 2 && args[1].equals("remove")) removeScreen(args, guild, message, channel, guildName);
		else addScreen(library, guild, message, channel, attachments, guildName, toDownload);
	}

	private static boolean isGoodChannel(MessageChannel channel) {
		return channel.getName().contains("screen-attaque") || channel.getName().contains("screen-défense");
	}
	
	private static boolean isRemoveScreenCommand(String[] args) {
		return args.length > 2 && args[1].equals("remove");
	}
	
	private static void removeScreen(String[] args, Message message) {
		MessageChannel channel = message.getChannel();
		message.addReaction("U+23F2").queue();
		channel.sendTyping().queue();
		Pattern pattern = Pattern.compile("^\\d+$");
		Matcher matcher;
		String find;
		List<Message> history = channel.getHistory().retrievePast(100).complete();
		List<String> removedMessages = new ArrayList<>();
		for(int i = 2; i < args.length; i++) {
			matcher = pattern.matcher(args[i]);
			if(matcher.find()) {
				find = matcher.group();
				for(Message context : history)
					if(context.getId().equals(args[i])) {
						List<MessageEmbed> embed = message.getEmbeds();
						if(embed.isEmpty()) removeScreenWithReference(context.getMessageReference());
						else removeScreenWithContext(embed, guild, guildName);
						removedMessages.add(find);
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
	
	private static void removeScreenWithReference(MessageReference reference) {
		if(reference == null) return;
		Message referenceMessage = reference.getMessage();
		Guild guild = referenceMessage.getGuild();
		User author = referenceMessage.getAuthor();
		String guildName = guild.getName(), authorName = author.getName();
		File toDownload = new File(guildName + "/" + authorName);
		referenceMessage
		.getAttachments()
		.stream()
		.filter(attachment -> attachment.isImage() && !toDownload.exists()).forEach(attachment -> {
			File file = downloadAttachment(attachment, toDownload);
			try {
				if(file != null && file.exists()) {
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
	
	public static File downloadAttachment(Attachment attachment, File file) {
		return attachment
				.downloadToFile(file)
				.exceptionally(error -> {
					if(file.exists()) file.delete();
		        	error.printStackTrace();
		        	return null;
		        })
				.join();
	}*/
}
