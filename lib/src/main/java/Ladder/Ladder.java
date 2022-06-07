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
	
	public static void update(String guildName, Guild guild, List<String> ocr, boolean defense, boolean add) throws ClassNotFoundException, IOException, InterruptedException {
		List<Player> data = Player.unserialize(guildName);
		ocr.stream().filter(pseudo -> 
			data.stream().filter(player -> player.getPseudo().equals(pseudo)).count() > 0)
		.forEach(pseudo -> {
			data.stream().filter(player -> player.getPseudo().equals(pseudo)).forEach(player -> {
				if(!defense) {
					if(add) {
						player.setNumberAttacksMonth(player.getNumberAttacksMonth() + 1);
						player.setNumberAttacksTotal(player.getNumberAttacksTotal() + 1);
					} else {
						player.setNumberAttacksMonth(player.getNumberAttacksMonth() - 1);
						player.setNumberAttacksTotal(player.getNumberAttacksTotal() - 1);
					}
				} else {
					if(add) {
						player.setNumberDefencesMonth(player.getNumberDefencesMonth() + 1);
						player.setNumberDefencesTotal(player.getNumberDefencesTotal() + 1);
					} else {
						player.setNumberDefencesMonth(player.getNumberDefencesMonth() - 1);
						player.setNumberDefencesTotal(player.getNumberDefencesTotal() - 1);
					}
				}
				if(add) {
					player.setMonthPoints(player.getMonthPoints() + 1);
					player.setTotalPoints(player.getTotalPoints() + 1);
				} else {
					player.setMonthPoints(player.getMonthPoints() - 1);
					player.setTotalPoints(player.getTotalPoints() - 1);
				}
			});
		});
		ocr.stream().filter(pseudo -> 
			data.stream().filter(player -> player.getPseudo().equals(pseudo)).count() == 0)
		.forEach(pseudo -> {
			Player player = new Player(pseudo);
			if(!defense) {
				player.setNumberAttacksMonth(1);
				player.setNumberAttacksTotal(1);
			} else {
				player.setNumberDefencesMonth(1);
				player.setNumberDefencesTotal(1);
			}
			player.setMonthPoints(1);
			player.setTotalPoints(1);
			data.add(player);
		});
		Player.serialize(guildName, data);
		refreshLadderBoar(guild, data);
	}

	public static void refreshLadderBoar(Guild guild, List<Player> data) {
		guild
		.getTextChannels()
		.stream()
		.filter(channel -> channel.getName().equals("classement"))
		.findFirst()
		.ifPresent(channel -> {
			EmbedBuilder info = new EmbedBuilder().setColor(0x0000FF);
			Map<String, Integer> general = new HashMap<>();
			Map<String, Integer> months = new HashMap<>();
			data.stream().filter(player -> !player.getId().equals("NO ID")).forEach(player -> {
				if(general.containsKey(player.getId())) {
					general.replace(player.getId(), general.get(player.getId()) + player.getTotalPoints());
					months.replace(player.getId(), months.get(player.getId()) + player.getMonthPoints());
				} else {
					general.put(player.getId(), player.getTotalPoints());
					months.put(player.getId(), player.getMonthPoints());
				}
			});
			Map<String, Integer> sortedGeneral = general.entrySet().stream()
				    .sorted(Entry.comparingByValue())
				    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			Map<String, Integer> sortedMonth = months.entrySet().stream()
				    .sorted(Entry.comparingByValue())
				    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
			try { setPositions(guild.getName(), sortedGeneral, sortedMonth); }
			catch (ClassNotFoundException | IOException | InterruptedException e) { e.printStackTrace(); }
			List<Message> history = MessageHistory.getHistoryFromBeginning(channel).complete().getRetrievedHistory();
			info.addField("GENERAL :", "\n" + getTop(guild, sortedGeneral), true);
			Date date= new Date();
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int month = cal.get(Calendar.MONTH);
			info.addField(new DateFormatSymbols().getMonths()[month].toUpperCase() + " :", "\n" + getTop(guild, sortedMonth), true);
			info.setFooter("Last update : " + DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()));
			if(history.size() == 0) channel.sendMessageEmbeds(info.build()).queue();
			else if(history.size() == 1) channel.editMessageEmbedsById(history.get(0).getIdLong(), info.build()).queue();
			else {
				channel.deleteMessages(history).queue();
				channel.sendMessageEmbeds(info.build()).queue();
			}
		});
	}
	
	private static void setPositions(String guildName, Map<String, Integer> sortedGeneral, Map<String, Integer> sortedMonth) throws ClassNotFoundException, IOException, InterruptedException {
		List<Player> players = Player.unserialize(guildName);
		players.stream().filter(player -> !player.getId().equals("NO ID"))
		.forEach(player -> {
			player.setGeneralLadderPosition(findPosition(player.getId(), sortedGeneral));
			player.setMonthLadderPosition(findPosition(player.getId(), sortedMonth));
		});
		Player.serialize(guildName, players);
	}

	private static int findPosition(String id, Map<String, Integer> sortedMap) {
		int position = 0;
		for(String key : sortedMap.keySet()) {
			if(key.equals(id)) break;
			position++;
		}
		return sortedMap.entrySet().size() - position;
	}

	private static String getTop(Guild guild, Map<String, Integer> classment) {
		int size = classment.entrySet().size() > LADDER_SIZE ? 20 : classment.entrySet().size();
		List<String> top = new ArrayList<>();
		String str = "";
		for(String key : classment.keySet()) top.add("<@" + key + ">" + " (" + classment.get(key) + " points)");
		for(int i = 0; i < size; i++) str = str + "#" + (i + 1) + " " + top.get(top.size() - 1 - i) + "\n";
		return str;
	}
}
