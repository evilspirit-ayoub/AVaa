package Ladder;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;

public class Ladder {

	public static final int LADDER_SIZE = 20;
	
	public static void update(Guild guild, List<String> players, boolean defense, int points) throws ClassNotFoundException, IOException, InterruptedException {
		String guildName = guild.getName();
		List<Player> data = Player.unserialize(guildName);
		players.stream().forEach(pseudo -> {
			data.stream()
			.filter(player -> player.getPseudo().equals(pseudo))
			.findFirst()
			.ifPresentOrElse(player -> {
				if(defense) {
					player.setNumberDefencesMonth(player.getNumberDefencesMonth() + points);
					player.setNumberDefencesTotal(player.getNumberDefencesTotal() + points);
				} else {
					player.setNumberAttacksMonth(player.getNumberAttacksMonth() + points);
					player.setNumberAttacksTotal(player.getNumberAttacksTotal() + points);
				}
				player.setMonthPoints(player.getMonthPoints() + points);
				player.setTotalPoints(player.getTotalPoints() + points);
			}, () -> {
				Player player = new Player(pseudo);
				if(defense) {
					player.setNumberDefencesMonth(points);
					player.setNumberDefencesTotal(points);
				} else {
					player.setNumberAttacksMonth(points);
					player.setNumberAttacksTotal(points);
				}
				player.setMonthPoints(points);
				player.setTotalPoints(points);
				data.add(player);
			});
		});
		Player.serialize(guild.getName(), data);
		refreshLadderBoar(guild);
	}
	
	public static void refreshLadderBoar(Guild guild) {
		guild
		.getTextChannels()
		.stream()
		.filter(channel -> channel.getName().equals("classement"))
		.findFirst()
		.ifPresent(channel -> {
			try {
				List<Player> data = Player.unserialize(guild.getName());
				EmbedBuilder info = new EmbedBuilder();
				Map<String, Integer> general = new HashMap<>();
				Map<String, Integer> month = new HashMap<>();
				data.stream().forEach(player -> {
					if(general.containsKey(player.getPseudo())) {
						general.replace(player.getPseudo(), general.get(player.getPseudo()) + player.getTotalPoints());
						month.replace(player.getPseudo(), month.get(player.getPseudo()) + player.getMonthPoints());
					} else {
						general.put(player.getPseudo(), player.getTotalPoints());
						month.put(player.getPseudo(), player.getMonthPoints());
					}
				});
				Map<String, Integer> sortedGeneral = sortMap(general);
				Map<String, Integer> sortedMonth = sortMap(month);
				updatePlayersPositions(guild, data, sortedGeneral, sortedMonth);
				info.addField("GENERAL :", collectClassment(guild, sortedGeneral), true);
				info.addField(getMonth() + " :", collectClassment(guild, sortedMonth), true);
				info.setFooter("Last update : " + DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()));
				List<Message> history = MessageHistory.getHistoryFromBeginning(channel).complete().getRetrievedHistory();
				if(history.size() == 0) channel.sendMessageEmbeds(info.build()).queue();
				else if(history.size() == 1) channel.editMessageEmbedsById(history.get(0).getIdLong(), info.build()).queue();
				else {
					channel.deleteMessages(history).queue();
					channel.sendMessageEmbeds(info.build()).queue();
				}
			} catch (ClassNotFoundException | IOException | InterruptedException e3) { e3.printStackTrace(); }
		});
	}
	
	private static Map<String, Integer> sortMap(Map<String, Integer> map) {
		return map.entrySet().stream()
			    .sorted(Entry.comparingByValue())
			    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	private static void updatePlayersPositions(Guild guild, List<Player> players, Map<String, Integer> sortedGeneral, Map<String, Integer> sortedMonth) throws ClassNotFoundException, IOException, InterruptedException {
		players.stream().forEach(player -> {
			player.setGeneralLadderPosition(findPosition(player.getPseudo(), sortedGeneral));
			player.setMonthLadderPosition(findPosition(player.getPseudo(), sortedMonth));
		});
		Player.serialize(guild.getName(), players);
	}

	private static int findPosition(String pseudo, Map<String, Integer> map) {
		int position = 0;
		for(String key : map.keySet()) {
			if(key.equals(pseudo)) break;
			position++;
		}
		return map.entrySet().size() - position;
	}

	private static String collectClassment(Guild guild, Map<String, Integer> classment) {
		int size = classment.entrySet().size() > LADDER_SIZE ? 20 : classment.entrySet().size();
		List<String> top = new ArrayList<>();
		String str = "";
		for(String key : classment.keySet()) top.add(key + " (" + classment.get(key) + " points)");
		for(int i = 0; i < size; i++) str = str + "#" + (i + 1) + " " + top.get(top.size() - 1 - i) + "\n";
		return str;
	}
	
	private static String getMonth() {
		Date date= new Date();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int month = cal.get(Calendar.MONTH);
		return new DateFormatSymbols().getMonths()[month].toUpperCase();
	}
}
