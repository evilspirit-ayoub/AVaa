package DSBot.database.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Ladder extends ListenerAdapter {
	
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
	private List<Float> points;
	private String date;
	
	public Ladder(List<String> discordIds, List<Integer> positions, List<Float> points, String date) {
		this.discordIds = discordIds;
		this.positions = positions;
		this.points = points;
		this.date = date;
	}
	
	public List<String> getDiscordIds() { return discordIds; }
	public void setDiscordIds(List<String> discordIds) { this.discordIds = discordIds; }
	public List<Integer> getPositions() { return positions; }
	public void setPositions(List<Integer> positions) { this.positions = positions; }
	public List<Float> getPoints() { return points; }
	public void setPoints(List<Float> points) { this.points = points; }
	public String getDate() { return date; }
	public void setDate(String date) { this.date = date; }
	public void insert() throws SQLException { ladderDAO.insertLadder(this); }
	public void delete() throws SQLException { ladderDAO.deleteLadder(date); }
	public void update() throws SQLException { ladderDAO.updateLadder(this); }
	public static void delete(String date) throws SQLException { ladderDAO.deleteLadder(date); }
	public static Ladder getLadderBydId(String id) throws SQLException { return ladderDAO.selectLadderBydId(id); }
	public static Ladder getLadderBydDate(String date) throws SQLException { return ladderDAO.selectLadderByDate(date); }
	public static List<Ladder> getAllLadders() throws SQLException { return ladderDAO.selectAllLadders(); }
	
	public static void updatePoints(Guild guild, List<String> pseudos, float points) throws SQLException {
		if(isNewMonth()) resetMonthPoints(guild);
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
	
	private static void resetMonthPoints(Guild guild) throws SQLException {
		List<User> users = User.getAllUsers();
		for(User user : users) {
			user.setMonthPoints(0);
			user.setNumberDefencesMonth(0);
			user.update();
		}
		updatePoisitonsForLinkedUsers();
		updateThisMonthLadder();
		refreshDiscordChannelLadder(guild);
		guild
		.getTextChannels()
		.stream()
		.filter(channel -> channel.getName().contains("screen-defense"))
		.findFirst()
		.ifPresent(channel -> channel.sendMessage("Le premier screen du mois vient d etre poste et valide. Le compteur de point et defense du mois en cours ont ete reinitialise.").queue());
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
		List<User> monthLadderUsers = User.getAllLinkedUsersGroupByDiscordIdSumMonthPointsdOrderByMonthPoints();
		if(monthLadderUsers.isEmpty()) return;
		String date = LocalDate.now().getMonth().toString() + "/" + LocalDate.now().getYear();
		Ladder thisMonthLadder = getLadderBydDate(date);
		List<String> discordIds = monthLadderUsers.stream().map(user -> user.getDiscordId()).toList();
		List<Integer> positions = monthLadderUsers.stream().map(user -> user.getMonthLadderPosition()).toList();
		List<Float> points = monthLadderUsers.stream().map(user -> user.getMonthPoints()).toList();
		if(thisMonthLadder == null) {
			Ladder ladder = new Ladder(discordIds, positions, points, date);
			ladder.insert();
		} else {
			thisMonthLadder.setDiscordIds(discordIds);
			thisMonthLadder.setPositions(positions);
			thisMonthLadder.setPoints(points);
			thisMonthLadder.update();
		}
	}
	
	public static void refreshDiscordChannelLadder(Guild guild) {
		guild
		.getTextChannels()
		.stream()
		.filter(channel -> channel.getName().contains("classement"))
		.findFirst()
		.ifPresent(channel -> {
			try {
				List<User> generalLadderUsers = User.getAllLinkedUsersGroupByDiscordIdSumTotalPointsdOrderByTotalPoints();
				List<User> monthLadderUsers = User.getAllLinkedUsersGroupByDiscordIdSumMonthPointsdOrderByMonthPoints();
				if(generalLadderUsers.isEmpty()) return;
				String general = generalLadderUsers
						.stream()
						.map(user -> {
							Member member = guild.getMemberById(user.getDiscordId());
							String a = "#" + user.getGeneralLadderPosition() + " " + (member == null ? "<@" + user.getDiscordId() + ">" : member.getUser().getAsMention()) + " (" + user.getTotalPoints() + " points)";
							return a;
						})
						.collect(Collectors.joining("\n"));
				String month = monthLadderUsers
						.stream()
						.map(user -> {
							Member member = guild.getMemberById(user.getDiscordId());
							String a = "#" + user.getMonthLadderPosition() + " " + (member == null ? "<@" + user.getDiscordId() + ">" : member.getUser().getAsMention()) + " (" + user.getMonthPoints() + " points)";
							return a;
						})
						.collect(Collectors.joining("\n"));
				List<Message> history = MessageHistory.getHistoryFromBeginning(channel).complete().getRetrievedHistory();
				if(history.size() == 1) history.get(0).delete().queue();
				else if(history.size() > 1) channel.deleteMessages(history).queue();
				sendEmbeds(channel, general, month);
			} catch (SQLException e) { e.printStackTrace(); }
		});
	}

	/*private static String guildsMonthLadder(List<User> monthLadderUsers, Guild guild) {
		Map<String, Float> unsorted = new HashMap<>();
		for(User user : monthLadderUsers) {
			Role guildRole = getGuildName(user, guild);
			String guildName = guildRole == null ? "" : guildRole.getName();
			if(unsorted.containsKey(guildName))
				unsorted.replace(guildName, unsorted.get(guildName) + user.getMonthPoints());
			else
				unsorted.put(guildName, user.getMonthPoints());
		}
		Map<String, Float> sorted = unsorted.entrySet().stream()
			    .sorted(Entry.comparingByValue())
			    .collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		String res ="";
		for(String g : sorted.keySet())
			res = "#" + findPosition(g, sorted) + " " + g + " (" + sorted.get(g) + " points)\n" + res;
		return res;
	}
	
	private static Role getGuildName(User user, Guild guild) {
		return guild
				.getMemberById(user.getDiscordId())
				.getRoles()
				.stream()
				.filter(role -> role.getPermissions().isEmpty())
				.findFirst()
				.orElse(null);
	}*/

	private static void sendEmbeds(TextChannel channel, String general, String month) {
		if(general.length() == 0 && month.length() == 0) return;
		EmbedBuilder info = new EmbedBuilder();
		general += "\n";
		month += "\n";
		String[] generalLines = general.split("\n");
		String[] monthLines = month.split("\n");
		int nbrFields = 0;
		String generalStr = "", monthStr = "";
		for(int i = 0; i < generalLines.length; i++) {
			if((generalStr.length() + generalLines[i].length() + 1) > MessageEmbed.VALUE_MAX_LENGTH
					|| (monthStr.length() + monthLines[i].length() + 1) > MessageEmbed.VALUE_MAX_LENGTH) {
				info.addField(nbrFields == 0 ? "GENERAL :" : "", generalStr, true);
				info.addField(nbrFields == 0 ? LocalDate.now().getMonth() + " :" : "", monthStr, true);
				channel.sendMessageEmbeds(info.build()).queue();
				info.clear();
				generalStr = "";
				monthStr = "";
				nbrFields++;
			} else {
				generalStr = generalStr + generalLines[i] + "\n";
				monthStr = monthStr + monthLines[i] + "\n";
			}
		}
		if(generalStr.length() != 0 || monthStr.length() != 0) {
			info.addField(nbrFields == 0 ? "GENERAL :" : "", generalStr, true);
			info.addField(nbrFields == 0 ? LocalDate.now().getMonth() + " :" : "", monthStr, true);
			channel.sendMessageEmbeds(info.build()).queue();
		}
	}
}
