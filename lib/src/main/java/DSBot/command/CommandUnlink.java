package DSBot.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import DSBot.Library;
import DSBot.database.model.Ladder;
import DSBot.database.model.User;
import DSBot.exception.DSBotException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandUnlink implements CommandExecutor {
	
	private static Message message;
	private static MessageChannel channel;
	private static List<Member> mentionnedMembers;
	private static String[] args;
	
	@Override
	public void run(MessageReceivedEvent event, Command command, Library library, String[] args)
			throws InterruptedException, ClassNotFoundException, IOException, SQLException, DSBotException {
		message = event.getMessage();
		channel = event.getChannel();
		mentionnedMembers = message.getMentionedMembers();
		CommandUnlink.args = args;
		unlink();
	}
	
	private void unlink() throws SQLException, DSBotException {
		channel.sendTyping().queue();
		EmbedBuilder info = new EmbedBuilder();
		if(args.length == 1) {
			info.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl());
			List<User> unlikedUsersList = User.getAllUnlinkedUsers();
			info.addField("Les pseudos non encore link :", unlikedUsersList.stream().map(user -> user.getPseudo()).collect(Collectors.joining(", ")), false);
		} else if(args.length < 3)
			throw new DSBotException(message, "Pour pouvoir unlink : !unlink @DiscordTag unlinkedName");
		else if(mentionnedMembers.isEmpty())
			throw new DSBotException(message, "Le membre " + args[1] + " n'existe pas sur ce serveur.");
		else {
			if(!authorizedToUnlink(message.getMember(), mentionnedMembers.get(0)))
				throw new DSBotException(message, "Non autorise pour la plebe.");
			List<String> toUnlink = new ArrayList<>();
			for(int i = 2; i < args.length; i++) {
				if(args[i].isEmpty()) continue;
				User user = User.getUserByPseudo(args[i]);
				if(user != null && user.getDiscordId() != null && user.getDiscordId().equals(mentionnedMembers.get(0).getId())) {
					user.setDiscordId(null);
					user.setGeneralLadderPosition(-1);
					user.setMonthLadderPosition(-1);
					user.update();
				}
				toUnlink.add(args[i]);
			}
			Ladder.updatePoisitonsForLinkedUsers();
			Ladder.updateThisMonthLadder();
			Ladder.refreshDiscordChannelLadder(message.getGuild());
			sendEmbeds(toUnlink.stream().collect(Collectors.joining(", ")));
			return;
		}
		message.replyEmbeds(info.build()).queue();
	}

	private static boolean authorizedToUnlink(Member messageSenderMember, Member linkedMember) {
		if(messageSenderMember.getId().equals(linkedMember.getId())) return true;
		if(messageSenderMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) return true;
		if(messageSenderMember.hasPermission(Permission.KICK_MEMBERS)) return true;
		if(messageSenderMember.hasPermission(Permission.BAN_MEMBERS)) return true;
		return false;
	}
	
	private static void sendEmbeds(String unlinks) {
		if(unlinks.length() == 0) return;
		EmbedBuilder info = new EmbedBuilder();
		info.setTitle("Les pseudos suivant ont ete unlink :");
		if(!message.getAuthor().getId().equals(mentionnedMembers.get(0).getUser().getId()))
			info.setAuthor(mentionnedMembers.get(0).getUser().getName(), null, mentionnedMembers.get(0).getUser().getEffectiveAvatarUrl());
		else
			info.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl());
		unlinks += ", ";
		String[] unlinksLines = unlinks.split(", ");
		String unlinksStr = "";
		for(int i = 0; i < unlinksLines.length; i++) {
			if((unlinksStr.length() + unlinksLines[i].length() + 1) > MessageEmbed.DESCRIPTION_MAX_LENGTH) {
				info.setDescription(unlinksStr);
				channel.sendMessageEmbeds(info.build()).queue();
				info.clear();
				unlinksStr = "";
			} else unlinksStr = unlinksStr + unlinksLines[i] + "\n";
		}
		if(unlinksStr.length() != 0) {
			info.setDescription(unlinksStr);
			channel.sendMessageEmbeds(info.build()).queue();
		}
	}
}