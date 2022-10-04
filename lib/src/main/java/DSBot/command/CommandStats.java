package DSBot.command;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import DSBot.Library;
import DSBot.database.model.User;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandStats implements CommandExecutor {
	
	private static Message message;
	private static MessageChannel channel;
	
	@Override
	public void run(MessageReceivedEvent event, Command command, Library library, String[] args)
			throws InterruptedException, ClassNotFoundException, IOException, SQLException {
		message = event.getMessage();
		channel = event.getChannel();
		stats();
	}

	private void stats() throws SQLException {
		channel.sendTyping().queue();
		EmbedBuilder info = new EmbedBuilder();
		List<Member> mentionnedMembers = message.getMentionedMembers();
		Member memberToGetInfos = null;
		if(mentionnedMembers.isEmpty()) {
			info.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl());
			memberToGetInfos = message.getMember();
		} else {
			info.setAuthor(mentionnedMembers.get(0).getUser().getName(), null, mentionnedMembers.get(0).getUser().getEffectiveAvatarUrl());
			memberToGetInfos = mentionnedMembers.get(0);
		}
		List<User> linkedPseudos = User.getAllUsersByDiscordId(memberToGetInfos.getId());
		if(!linkedPseudos.isEmpty()) {
			info.addField("Name : ", memberToGetInfos.getUser().getName(), true);
			info.addField("General position : ", String.valueOf(linkedPseudos.get(0).getGeneralLadderPosition()), true);
			info.addField("Month position : ", String.valueOf(linkedPseudos.get(0).getMonthLadderPosition()), true);
			info.addField("Total points : ", String.valueOf(linkedPseudos.stream().mapToDouble(user -> user.getTotalPoints()).sum()), true);
			info.addField("Month points : ", String.valueOf(linkedPseudos.stream().mapToDouble(user -> user.getMonthPoints()).sum()), true);
			info.addField("Total defenses : ", String.valueOf(linkedPseudos.stream().mapToInt(user -> user.getNumberDefencesTotal()).sum()), true);
			info.addField("Month defenses : ", String.valueOf(linkedPseudos.stream().mapToInt(user -> user.getNumberDefencesMonth()).sum()), true);
		} else info.setTitle("Le membre " + memberToGetInfos.getUser().getName() + " doit être link à au moins un pseudo pour pouvoir récupérer ses informations.");
		message.replyEmbeds(info.build()).queue();
	}
}
