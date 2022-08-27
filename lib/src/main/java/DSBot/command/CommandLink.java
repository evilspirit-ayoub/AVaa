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
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandLink implements CommandExecutor {
	
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
		CommandLink.args = args;
		link();
	}
	
	private void link() throws DSBotException, SQLException {
		channel.sendTyping().queue();
		EmbedBuilder info = new EmbedBuilder();
		if(args.length < 2) {
			info.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl());
			List<User> users = User.getAllUsersByDiscordId(message.getMember().getId());
			if(users.isEmpty()) info.setTitle("Tu dois etre link a au moins un pseudo pour pouvoir voir tes links.");
			else info.addField("Mes links : ", users.stream().map(user -> user.getPseudo()).collect(Collectors.joining(", ")), false);
		} else {
			if(mentionnedMembers.isEmpty())
				throw new DSBotException(message, "Le membre " + args[1] + " n'existe pas sur ce serveur.");
			info.setAuthor(mentionnedMembers.get(0).getUser().getName(), null, mentionnedMembers.get(0).getUser().getEffectiveAvatarUrl());
			if(args.length == 2) {
				List<User> users = User.getAllUsersByDiscordId(mentionnedMembers.get(0).getId());
				if(users.isEmpty()) info.setTitle("Le membre " + mentionnedMembers.get(0).getUser().getName() + " n'a pas de link.");
				else info.addField("Les links de " + mentionnedMembers.get(0).getUser().getName() + " :", users.stream().map(user -> user.getPseudo()).collect(Collectors.joining(", ")), false);
			} else {
				if(!authorizedToLink(message.getMember(), mentionnedMembers.get(0)))
					throw new DSBotException(message, "Non autorise pour la plebe.");
				if(!message.getAuthor().getId().equals(mentionnedMembers.get(0).getUser().getId()))
					info.setAuthor(mentionnedMembers.get(0).getUser().getName(), null, mentionnedMembers.get(0).getUser().getEffectiveAvatarUrl());
				else
					info.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl());
				List<String> newLinked = new ArrayList<>();
				List<String> alreadyLinked = new ArrayList<>();
				for(int i = 2; i < args.length; i++) {
					if(args[i].isEmpty()) continue;
					if(!User.isLinked(args[i])) {
						if(User.isUserExist(args[i])) {
							User user = User.getUserByPseudo(args[i]);
							user.setDiscordId(mentionnedMembers.get(0).getId());
							user.update();
						} else new User(mentionnedMembers.get(0).getId(), args[i], -1, -1, 0, 0, 0, 0).insert();
						newLinked.add(args[i]);
					} else alreadyLinked.add(args[i]);
				}
				Ladder.updatePoisitonsForLinkedUsers();
				Ladder.updateThisMonthLadder();
				Ladder.refreshDiscordChannelLadder(message.getGuild());
				info.addField("Nouveaux pseudos link :", newLinked.stream().collect(Collectors.joining(", ")), false);
				if(!alreadyLinked.isEmpty()) info.addField("Pseudo deja link sur un autre membre :", alreadyLinked.stream().collect(Collectors.joining(", ")), false);
			}
		}
		message.replyEmbeds(info.build()).queue();
	}

	private static boolean authorizedToLink(Member messageSenderMember, Member linkedMember) {
		if(messageSenderMember.getId().equals(linkedMember.getId())) return true;
		if(messageSenderMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) return true;
		if(messageSenderMember.hasPermission(Permission.KICK_MEMBERS)) return true;
		if(messageSenderMember.hasPermission(Permission.BAN_MEMBERS)) return true;
		return false;
	} 
}
