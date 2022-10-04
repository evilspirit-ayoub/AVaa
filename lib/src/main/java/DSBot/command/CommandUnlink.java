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
		if(args.length == 1) {
			List<User> unlikedUsersList = User.getAllUnlinkedUsers();
			sendEmbeds(new EmbedBuilder(), "Les pseudos non encore link :", unlikedUsersList.stream().map(user -> user.getPseudo()).collect(Collectors.joining(", ")));
		} else if(args.length < 3)
			throw new DSBotException(message, "Pour pouvoir unlink : !unlink @DiscordTag unlinkedName");
		else if(mentionnedMembers.isEmpty())
			throw new DSBotException(message, "Le membre " + args[1] + " n'existe pas sur ce serveur.");
		else {
			if(!authorizedToUnlink(message.getMember(), mentionnedMembers.get(0)))
				throw new DSBotException(message, "Non autoris� pour la pl�be.");
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
			sendEmbeds(new EmbedBuilder(), "Les pseudos suivants ont �t� unlink :", toUnlink.stream().collect(Collectors.joining(", ")));
			if(!toUnlink.isEmpty()) {
				channel.sendMessage("?unlink " + mentionnedMembers.get(0).getAsMention() + " " + toUnlink.stream().collect(Collectors.joining(" "))).queue();
			}
			return;
		}
	}

	private static boolean authorizedToUnlink(Member messageSenderMember, Member linkedMember) {
		if(messageSenderMember.getId().equals("257273362974375937")) return true;
		if(messageSenderMember.getId().equals(linkedMember.getId())) return true;
		if(messageSenderMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) return true;
		if(messageSenderMember.hasPermission(Permission.KICK_MEMBERS)) return true;
		if(messageSenderMember.hasPermission(Permission.BAN_MEMBERS)) return true;
		return false;
	}
	
	private static void sendEmbeds(EmbedBuilder info, String title, String unlinks) {
		if(unlinks.length() == 0) return;
		if(!mentionnedMembers.isEmpty() && message.getAuthor().getId().equals(mentionnedMembers.get(0).getUser().getId()))
			info.setAuthor(mentionnedMembers.get(0).getUser().getName(), null, mentionnedMembers.get(0).getUser().getEffectiveAvatarUrl());
		else
			info.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl());
		unlinks += ", ";
		String[] unlinksLines = unlinks.split(", ");
		String unlinksStr = "";
		int nbrEmbeds = 0;
		int pos = 0;
		while(pos < unlinksLines.length) {
			if((unlinksStr.length() + unlinksLines[pos].length() + 2) > MessageEmbed.DESCRIPTION_MAX_LENGTH) {
				if(nbrEmbeds == 0) info.setTitle(title);
				nbrEmbeds ++;
				info.setDescription(unlinksStr);
				channel.sendMessageEmbeds(info.build()).queue();
				info.clear();
				unlinksStr = "";
			} else {
				unlinksStr = unlinksStr + unlinksLines[pos] + ", ";
				pos++;
			}
		}
		if(unlinksStr.length() != 0) {
			info.setDescription(unlinksStr);
			channel.sendMessageEmbeds(info.build()).queue();
		}
	}
}