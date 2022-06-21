package DSBot.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import DSBot.Library;
import DSBot.database.model.Ladder;
import DSBot.database.model.User;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandUnlink implements CommandExecutor {
	
	private static boolean authorizedToUnlink(Member messageSenderMember, Member linkedMember) {
		if(messageSenderMember.getId().equals(linkedMember.getId())) return true;
		if(messageSenderMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) return true;
		if(messageSenderMember.hasPermission(Permission.KICK_MEMBERS)) return true;
		if(messageSenderMember.hasPermission(Permission.BAN_MEMBERS)) return true;
		return false;
	}

	@Override
	public void run(MessageReceivedEvent event, Command command, Library library, String[] args) throws InterruptedException, ClassNotFoundException, IOException, SQLException {
		Message message = event.getMessage();
		message.getChannel().sendTyping().queue();
		EmbedBuilder info = new EmbedBuilder();
		List<Member> mentionnedMembers = message.getMentionedMembers();
		if(args.length == 1) {
			info.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl());
			List<User> unlikedUsersList = User.getAllUnlinkedUsers();
			info.addField("Les pseudos non encore linké :", unlikedUsersList.stream().map(user -> user.getPseudo()).collect(Collectors.joining(", ")), false);
		} else if(args.length < 3) {
			info.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl());
			info.setTitle("Pour pouvoir unlink : !unlink @DiscordTag unlinkedName");
		}
		else if(mentionnedMembers.isEmpty()) {
			info.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl());
			info.setTitle("Le membre " + args[1] + " n'existe pas sur ce serveur.");
		}
		else {
			info.setAuthor(mentionnedMembers.get(0).getUser().getName(), null, mentionnedMembers.get(0).getUser().getEffectiveAvatarUrl());
			if(authorizedToUnlink(message.getMember(), mentionnedMembers.get(0))) {
				List<String> toUnlink = new ArrayList<>();
				for(int i = 2; i < args.length; i++) {
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
				info.addField("Les pseudos suivant ont été unlink :", toUnlink.stream().collect(Collectors.joining(", ")), false);
			} else info.setTitle("Non autorisé.");
		}
		message.replyEmbeds(info.build()).queue();
	}
}