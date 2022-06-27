package DSBot.exception;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;

public class DSBotException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public DSBotException(Message context, String error) {
		super(error);
		EmbedBuilder info = new EmbedBuilder();
		info.setAuthor(context.getAuthor().getName(), null, context.getAuthor().getEffectiveAvatarUrl());
		info.setTitle("Error :");
		info.setDescription(error);
		context.replyEmbeds(info.build()).queue();
	}
}
