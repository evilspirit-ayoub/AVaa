package DSBot.database.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import DSBot.database.dao.LadderDAO;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;

public class Ladder {
	
	public final static LadderDAO ladderDAO;
	static {
		final String PATH_FILE_PROPERTIES = "conf.properties";
		Properties properties = new Properties();
		try (FileInputStream fis = new FileInputStream(PATH_FILE_PROPERTIES);) { properties.load(fis);}
		catch (IOException e) { e.printStackTrace(); }
		ladderDAO = new LadderDAO(properties.getProperty("jdbc.driver.class"), properties.getProperty("jdbc.url"), properties.getProperty("jdbc.username"), properties.getProperty("jdbc.password"));
	}
	
	private List<String> discordIds;
	private List<Integer> positions;
	private String date;
	
	public Ladder(List<String> discordIds, List<Integer> positions, String date) {
		this.discordIds = discordIds;
		this.positions = positions;
		this.date = date;
	}
	
	public List<String> getDiscordIds() { return discordIds; }
	public void setDiscordIds(List<String> discordIds) { this.discordIds = discordIds; }
	public List<Integer> getPositions() { return positions; }
	public void setPositions(List<Integer> positions) { this.positions = positions; }
	public String getDate() { return date; }
	public void setDate(String date) { this.date = date; }
	public void insert() throws SQLException { ladderDAO.insertLadder(this); }
	public void delete() throws SQLException { ladderDAO.deleteLadder(date); }
	public void update() throws SQLException { ladderDAO.updateLadder(this); }
	public static void delete(String date) throws SQLException { ladderDAO.deleteLadder(date); }
	public static Ladder getLadderBydId(String id) throws SQLException { return ladderDAO.selectLadderBydId(id); }
	public static Ladder getLadderBydDate(String date) throws SQLException { return ladderDAO.selectLadderByDate(date); }
	public static List<Ladder> getAllLadders() throws SQLException { return ladderDAO.selectAllLadders(); }
	
	public static void update(Guild guild, List<String> pseudos, float points) throws SQLException {
		if(isNewMonth()) resetMonthPoints();
		for(String pseudo : pseudos) {
			User user = User.getUserByPseudo(pseudo);
			if(user == null) {
				User newUser = new User(null, pseudo, -1, -1, points, points, 1, 1);
				newUser.insert();
			} else {
				user.setTotalPoints(user.getTotalPoints() + points);
				user.setMonthPoints(user.getMonthPoints() + points);
				if(points < 0) {
					user.setNumberDefencesTotal(user.getNumberDefencesTotal() - 1);
					user.setNumberDefencesMonth(user.getNumberDefencesMonth() - 1);
				} else {
					user.setNumberDefencesTotal(user.getNumberDefencesTotal() + 1);
					user.setNumberDefencesMonth(user.getNumberDefencesMonth() + 1);
				}
				user.update();
			}
		}
		updatePoisitonsForLinkedUsers();
		updateThisMonthLadder();
		refreshDiscordChannelLadder(guild);
	}

	public static void updatePoisitonsForLinkedUsers() throws SQLException {
		List<User> linkedUsers = User.getAllLinkedUsers();
		if(linkedUsers.isEmpty()) return;
		Map<String, Float> unsortedGeneralPositions = new HashMap<>();
		Map<String, Float> unsortedMonthPositions = new HashMap<>();
		linkedUsers.stream()
		.forEach(user -> {
			if(unsortedGeneralPositions.containsKey(user.getDiscordId())) {
				unsortedGeneralPositions.replace(user.getDiscordId(), unsortedGeneralPositions.get(user.getDiscordId()) + user.getTotalPoints());
				unsortedMonthPositions.replace(user.getDiscordId(), unsortedMonthPositions.get(user.getDiscordId()) + user.getMonthPoints());
			} else {
				unsortedGeneralPositions.put(user.getDiscordId(), user.getTotalPoints());
				unsortedMonthPositions.put(user.getDiscordId(), user.getMonthPoints());
			}
		});
		Map<String, Float> sortedGeneralPositions = sortMapByDouble(unsortedGeneralPositions);
		Map<String, Float> sortedMonthPositions = sortMapByDouble(unsortedMonthPositions);
		linkedUsers.stream()
		.forEach(user -> {
			List<User> linkeds = new ArrayList<>();
			try { linkeds = User.getAllUsersByDiscordId(user.getDiscordId()); }
			catch (SQLException e) { e.printStackTrace(); }
			int generalPosition = findPosition(user.getDiscordId(), sortedGeneralPositions);
			int monthPosition = findPosition(user.getDiscordId(), sortedMonthPositions);
			linkeds.stream()
			.forEach(pseudo -> {
				pseudo.setGeneralLadderPosition(generalPosition);
				pseudo.setMonthLadderPosition(monthPosition);
				try { pseudo.update(); } catch (SQLException e) { e.printStackTrace(); }
			});
		});
	}

	private static boolean isNewMonth() throws SQLException {
		Screen lastScreen = Screen.getLastScreen();
		return lastScreen == null ? false : !lastScreen.getDate().getMonth().equals(LocalDate.now().getMonth());
	}
	
	private static void resetMonthPoints() throws SQLException {
		List<User> users = User.getAllUsers();
		for(User user : users) {
			user.setMonthPoints(0);
			user.update();
		}
	}
	
	private static Map<String, Float> sortMapByDouble(Map<String, Float> map) {
		return map.entrySet().stream()
			    .sorted(Entry.comparingByValue())
			    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}
	
	private static int findPosition(String discordId, Map<String, Float> map) {
		int count = 0;
		Float points = map.get(discordId);
		List<Float> pts = new ArrayList<>();
		for(String id : map.keySet())
			if(!pts.contains(map.get(id))) pts.add(map.get(id));
		for(Float p : pts) if(p > points) count++;
		return count + 1;
	}
	
	public static void updateThisMonthLadder() throws SQLException {
		List<User> monthLadderUsers = User.getAllLinkedUsersGroupByDiscordIdOrderBy("monthLadderPosition");
		if(monthLadderUsers.isEmpty()) return;
		String date = LocalDate.now().getMonth().toString() + "/" + LocalDate.now().getYear();
		Ladder thisMonthLadder = getLadderBydDate(date);
		List<String> discordIds = monthLadderUsers.stream().map(user -> user.getDiscordId()).toList();
		List<Integer> positions = monthLadderUsers.stream().map(user -> user.getMonthLadderPosition()).toList();
		if(thisMonthLadder == null) {
			Ladder ladder = new Ladder(discordIds, positions, date);
			ladder.insert();
		} else {
			thisMonthLadder.setDiscordIds(discordIds);
			thisMonthLadder.setPositions(positions);
			thisMonthLadder.update();
		}
	}
	
	public static void refreshDiscordChannelLadder(Guild guild) {
		guild
		.getTextChannels()
		.stream()
		.filter(channel -> channel.getName().equals("classement"))
		.findFirst()
		.ifPresent(channel -> {
			try {
				EmbedBuilder info = new EmbedBuilder();
				List<User> allLinkedUsers = User.getAllLinkedUsersGroupByDiscordId();
				Map<String, Float> unsortedGeneralPositions = new HashMap<>();
				Map<String, Float> unsortedMonthPositions = new HashMap<>();
				allLinkedUsers.stream()
				.forEach(user -> {
					try {
						List<User> links = User.getAllUsersByDiscordId(user.getDiscordId());
						unsortedGeneralPositions.put(user.getDiscordId(), (float) 0);
						unsortedMonthPositions.put(user.getDiscordId(), (float) 0);
						for(User link : links) {
							unsortedGeneralPositions.replace(user.getDiscordId(), unsortedGeneralPositions.get(user.getDiscordId()) + link.getTotalPoints());
							unsortedMonthPositions.replace(user.getDiscordId(), unsortedMonthPositions.get(user.getDiscordId()) + link.getMonthPoints());
						}
					} catch (SQLException e) { e.printStackTrace(); }
				});
				Map<String, Float> sortedGeneralPositions = sortMapByDouble(unsortedGeneralPositions);
				Map<String, Float> sortedMonthPositions = sortMapByDouble(unsortedMonthPositions);
				List<String> ladder = new ArrayList<>();
				sortedGeneralPositions
				.forEach((id, points) -> ladder.add("#" + findPosition(id, sortedGeneralPositions) + " " + guild.getMemberById(id).getUser().getAsMention() + " (" + points + " points)"));
				Collections.reverse(ladder);
				info.addField("GENERAL :", ladder.stream().collect(Collectors.joining("\n")), true);
				ladder.clear();
				sortedMonthPositions
				.forEach((id, points) -> ladder.add("#" + findPosition(id, sortedMonthPositions) + " " + guild.getMemberById(id).getUser().getAsMention() + " (" + points + " points)"));
				Collections.reverse(ladder);
				info.addField(LocalDate.now().getMonth() + " :", ladder.stream().collect(Collectors.joining("\n")), true);
				info.setFooter("Last update : " + DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss").format(LocalDateTime.now()));
				List<Message> history = MessageHistory.getHistoryFromBeginning(channel).complete().getRetrievedHistory();
				if(history.size() == 0) channel.sendMessageEmbeds(info.build()).queue();
				else if(history.size() == 1) channel.editMessageEmbedsById(history.get(0).getIdLong(), info.build()).queue();
				else {
					channel.deleteMessages(history).queue();
					channel.sendMessageEmbeds(info.build()).queue();
				}
			} catch (SQLException e) { e.printStackTrace(); }
		});
	}
}
