package DSBot.command;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import Ladder.Player;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;

public class LinkCommand {

	public static void link(String args[], Message message) throws ClassNotFoundException, IOException, InterruptedException {
		message.getChannel().sendTyping().queue();
		EmbedBuilder info = new EmbedBuilder().setColor(0xf45642);
		List<Member> mentionnedMembers = message.getMentionedMembers();
		if(args.length < 2) info.setTitle("To make a link, it must be : !link @DiscordTag linkedName\nTo see the link of a user : !link @DiscordTag");
		else if(mentionnedMembers.isEmpty()) info.setTitle("Member " + args[1] + " not found.");
		else {
			String guildName = message.getGuild().getName();
			List<Player> players = Player.unserialize(guildName);
			List<String> linkedNames = new ArrayList<>();
			if(args.length == 2) {
				info.setTitle("Linked names :");
				players
				.stream()
				.filter(player -> player.getId().equals(mentionnedMembers.get(0).getId()))
				.forEach(player -> linkedNames.add(player.getPseudo()));
				info.setDescription(linkedNames.stream().collect(Collectors.joining("\n")));
			} else {
				info.setTitle("Added linked names :");
				for(int i = 2; i < args.length; i++) linkedNames.add(args[i]);
				List<String> associatedLink = new ArrayList<>();
				players
				.stream()
				.forEach(player -> {
					if(linkedNames.contains(player.getPseudo())) {
						linkedNames.remove(player.getPseudo());
						if(player.getId().equals("NO ID")) {
							associatedLink.add(player.getPseudo());
							player.setId(mentionnedMembers.get(0).getId());
						}
					}
				});
				for(String link : linkedNames) players.add(new Player(mentionnedMembers.get(0).getId(), link));
				Player.serialize(guildName, players);
				//Ladder.refreshLadderBoar(guild, players);
				String description = associatedLink.stream().collect(Collectors.joining("\n")) + linkedNames.stream().collect(Collectors.joining("\n"));
				info.setDescription(description.isEmpty() ? "Nothing's added." : description);
			}
		}
		message.replyEmbeds(info.build()).queue();
	}
}
