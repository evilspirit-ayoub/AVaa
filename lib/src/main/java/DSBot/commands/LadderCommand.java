package DSBot.commands;

import java.io.IOException;
import java.util.List;

import Ladder.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class LadderCommand {
	
	public static  void ladder(String guildName, String[] args, Message message, MessageChannel channel) throws ClassNotFoundException, IOException, InterruptedException {
		EmbedBuilder info = new EmbedBuilder().setColor(0x0000FF).setFooter("Libérez moi svp");
		List<Member> mentionnedMembers = message.getMentionedMembers();
		if(args.length > 1) info.setTitle("Member " + args[1] + " not found.");
		else {
			User user = mentionnedMembers.isEmpty() ? message.getAuthor() : mentionnedMembers.get(0).getUser();
			Player.unserialize(guildName).stream().filter(player -> player.getId().equals(user.getId()))
			.findFirst()
			.ifPresentOrElse(player -> {
				info.addField("Name : ", user.getName(), true);
				info.addField("General position : ", String.valueOf(player.getGeneralLadderPosition()), true);
				info.addField("Month position : ", String.valueOf(player.getMonthLadderPosition()), true);
				info.addField("Total points : ", String.valueOf(player.getTotalPoints()), true);
				info.addField("Month points : ", String.valueOf(player.getMonthPoints()), true);
				info.addField("Total attacks : ", String.valueOf(player.getNumberAttacksTotal()), true);
				info.addField("Month attacks : ", String.valueOf(player.getNumberAttacksMonth()), true);
				info.addField("Total defenses : ", String.valueOf(player.getNumberDefencesTotal()), true);
				info.addField("Month defenses : ", String.valueOf(player.getNumberDefencesMonth()), true);
			}, () -> info.setTitle("Member " + user.getName() + " not linked. Must be linked to get ladder info"));
		}
		message.replyEmbeds(info.build()).queue();
	}
}
