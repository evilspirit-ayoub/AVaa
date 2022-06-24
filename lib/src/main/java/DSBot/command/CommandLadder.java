package DSBot.command;

import java.io.IOException;
import java.time.Month;
import java.time.Year;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import DSBot.Library;
import DSBot.database.model.Ladder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandLadder implements CommandExecutor {

	@Override
	public void run(MessageReceivedEvent event, Command command, Library library, String[] args) throws InterruptedException, ClassNotFoundException, IOException, Exception {
		Message message = event.getMessage();
		message.getChannel().sendTyping().queue();
		EmbedBuilder info = new EmbedBuilder();
		info.setAuthor(message.getAuthor().getName(), null, message.getAuthor().getEffectiveAvatarUrl());
		if(args.length < 2) info.setTitle("Doit etre de la forme : !ladder numeroDuMois/annee");
		else if(!isGoodDate(args[1])) info.setTitle("Mauvaise date. Doit etre de la forme : !ladder numeroDuMois/annee");
		else {
			String month = Month.of(Integer.valueOf(args[1].split("/")[0])).toString();
			String year = Year.of(Integer.valueOf(args[1].split("/")[1])).toString();
			Ladder ladder = Ladder.getLadderBydDate(month + "/" + year);
			if(ladder == null) info.setTitle("Le ladder du " + month + "/" + year + " n'existe pas.");
			else {
				Map<String, Integer> unsortedLadder = new HashMap<>();
				ladder.getDiscordIds().stream()
				.forEach(pseudo -> {
					int position = ladder.getDiscordIds().indexOf(pseudo);
					if(!unsortedLadder.containsKey(pseudo)) {
						unsortedLadder.put(pseudo, ladder.getPositions().get(position));
					}
				});
				Map<String, Integer> sortedLadder = sortMap(unsortedLadder);
				String display = "";
				for(String id : sortedLadder.keySet()) {
					User user = message.getGuild().getMemberById(id).getUser();
					display = "\n#" + sortedLadder.get(id) + " " + (user == null ? id : user.getName()) + display;
				}
				info.setTitle(display);
			}
		}
		message.replyEmbeds(info.build()).queue();
	}

	private boolean isGoodDate(String date) {
		Pattern pattern = Pattern.compile("^\\d+$");
		Matcher matcher;
		String[] split = date.split("/");
		if(split.length != 2 || split[0].length() > 2 || split[1].length() > 4) return false;
		matcher = pattern.matcher(split[0]);
		if(!matcher.find()) return false;
		matcher = pattern.matcher(split[1]);
		if(!matcher.find()) return false;
		return true;
	}
	
	private static Map<String, Integer> sortMap(Map<String, Integer> map) {
		return map.entrySet().stream()
			    .sorted(Entry.comparingByValue())
			    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}
}
