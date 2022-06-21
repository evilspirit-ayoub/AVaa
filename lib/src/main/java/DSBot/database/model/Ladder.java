package DSBot.database.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
	
	private List<String> pseudos;
	private List<Integer> positions;
	private String date;
	
	public Ladder(List<String> pseudos, List<Integer> positions, String date) {
		this.pseudos = pseudos;
		this.positions = positions;
		this.date = date;
	}
	
	public List<String> getPseudos() { return pseudos; }
	public void setPseudos(List<String> pseudos) { this.pseudos = pseudos; }
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
	
	public static void update(Guild guild, List<String> pseudos, double points) throws SQLException {
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
		Map<String, Double> unsortedGeneralPositions = new HashMap<>();
		Map<String, Double> unsortedMonthPositions = new HashMap<>();
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
		Map<String, Double> sortedGeneralPositions = sortMap(unsortedGeneralPositions);
		Map<String, Double> sortedMonthPositions = sortMap(unsortedMonthPositions);
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
	
	private static Map<String, Double> sortMap(Map<String, Double> map) {
		return map.entrySet().stream()
			    .sorted(Entry.comparingByValue())
			    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}
	
	private static int findPosition(String discordId, Map<String, Double> map) {
		int position = 0;
		for(String key : map.keySet()) {
			if(key.equals(discordId)) break;
			position++;
		}
		return map.entrySet().size() - position;
	}
	
	public static void updateThisMonthLadder() throws SQLException {
		List<User> monthLadderUsers = User.getAllLinkedUsersGroupByDiscordIdOrderBy("monthLadderPosition");
		List<String> pseudos = monthLadderUsers.stream().map(user -> user.getPseudo()).toList();
		if(pseudos.isEmpty()) return;
		List<Integer> positions = monthLadderUsers.stream().map(user -> user.getMonthLadderPosition()).toList();
		String date = LocalDate.now().getMonth().toString() + "/" + LocalDate.now().getYear();
		Ladder thisMonthLadder = getLadderBydDate(date);
		if(thisMonthLadder == null) {
			Ladder ladder = new Ladder(pseudos, positions, date);
			ladder.insert();
		} else {
			thisMonthLadder.setPseudos(pseudos);
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
			EmbedBuilder info = new EmbedBuilder();
			try {
				List<User> generalLadderUsers = User.getAllLinkedUsersGroupByDiscordIdOrderBy("generalLadderPosition");
				List<User> monthLadderUsers = User.getAllLinkedUsersGroupByDiscordIdOrderBy("monthLadderPosition");
				info.addField("GENERAL :",
						generalLadderUsers.stream().map(user -> "#" + (generalLadderUsers.indexOf(user) + 1) + " " + guild.getMemberById(user.getDiscordId()).getAsMention() +  " (" + user.getTotalPoints() + " points)").collect(Collectors.joining("\n")),
						true);
				info.addField(LocalDate.now().getMonth() + " :",
						monthLadderUsers.stream().map(user -> "#" + (monthLadderUsers.indexOf(user) + 1) + " " + guild.getMemberById(user.getDiscordId()).getAsMention() + " (" + user.getMonthPoints() + " points)").collect(Collectors.joining("\n")),
						true);
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
