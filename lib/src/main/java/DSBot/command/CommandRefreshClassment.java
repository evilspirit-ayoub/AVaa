package DSBot.command;

import java.io.IOException;
import java.sql.SQLException;

import DSBot.Library;
import DSBot.database.model.Ladder;
import DSBot.exception.DSBotException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandRefreshClassment implements CommandExecutor {
	
	private static Message message;
	private static MessageChannel channel;
	
	@Override
	public void run(MessageReceivedEvent event, Command command, Library library, String[] args)
			throws InterruptedException, ClassNotFoundException, IOException, Exception {
		message = event.getMessage();
		channel = event.getChannel();
		refreshClassment();
	}
	
	private void refreshClassment() throws DSBotException, SQLException {
		channel.sendTyping().queue();
		if(!authorizedToRefreshClassment(message.getMember()))
			throw new DSBotException(message, "Non autorise pour la plebe.");
		Ladder.updatePoisitonsForLinkedUsers();
		Ladder.updateThisMonthLadder();
		Ladder.refreshDiscordChannelLadder(message.getGuild());
		EmbedBuilder info = new EmbedBuilder();
		info.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl());
		info.setTitle("Classement mis a jour.");
		message.replyEmbeds(info.build()).queue();
	}

	private static boolean authorizedToRefreshClassment(Member messageSenderMember) {
		if(messageSenderMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) return true;
		if(messageSenderMember.hasPermission(Permission.KICK_MEMBERS)) return true;
		if(messageSenderMember.hasPermission(Permission.BAN_MEMBERS)) return true;
		return false;
	}
}
