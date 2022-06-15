package DSBot.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import Ladder.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class UnlinkCommand {

	public static void unlink(String[] args, Message message) throws ClassNotFoundException, IOException, InterruptedException {
		message.getChannel().sendTyping().queue();
		EmbedBuilder info = new EmbedBuilder().setColor(0xf45642);
		String guildName = message.getGuild().getName();
		List<Member> mentionnedMembers = message.getMentionedMembers();
		if(args.length == 1) {
			info.setTitle("Unlinked list :");
			List<String> unlinkedList = new ArrayList<>();
			List<Player> players = Player.unserialize(guildName);
			players
			.stream()
			.filter(player -> player.getId().equals("NO ID"))
			.forEach(player -> unlinkedList.add(player.getPseudo()));
			info.setDescription(unlinkedList.stream().collect(Collectors.joining("\n")));
		} else if(args.length < 3) info.setTitle("To make a unlink, it must be : !unlink @DiscordTag unlinkedName");
		else if(mentionnedMembers.isEmpty()) info.setTitle("Member " + args[1] + " not exist.");
		else {
			info.setTitle("Unlinked names :");
			List<Player> players = Player.unserialize(guildName);
			List<String> unlinkedNames = new ArrayList<>();
			for(int i = 2; i < args.length; i++) unlinkedNames.add(args[i]);
			List<String> unassociatedLink = new ArrayList<>();
			players
			.stream()
			.filter(player -> player.getId().equals(mentionnedMembers.get(0).getUser().getId()) && unlinkedNames.contains(player.getPseudo()))
			.forEach(player -> {
				player.setId("NO ID");
				unassociatedLink.add(player.getPseudo());
			});
			Player.serialize(guildName, players);
			//Ladder.refreshLadderBoar(guild, players);
			String description = unassociatedLink.stream().collect(Collectors.joining("\n"));
			info.setDescription(description.isEmpty() ? "Nothing's unlinked." : description);
		}
		message.replyEmbeds(info.build()).queue();
	}
}
