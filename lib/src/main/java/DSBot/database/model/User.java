package DSBot.database.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import DSBot.database.dao.UsersDAO;

public class User {

	public final static UsersDAO usersDAO;
	static {
		final String PATH_FILE_PROPERTIES = "conf.properties";
		Properties properties = new Properties();
		try (FileInputStream fis = new FileInputStream(PATH_FILE_PROPERTIES);) { properties.load(fis);}
		catch (IOException e) { e.printStackTrace(); }
		usersDAO = new UsersDAO(properties.getProperty("jdbc.driver.class"), properties.getProperty("jdbc.url"), properties.getProperty("jdbc.username"), properties.getProperty("jdbc.password"));
	}
	
	private String discordId, pseudo;
	private int generalLadderPosition, monthLadderPosition;
	private float totalPoints, monthPoints;
	private int numberDefencesTotal, numberDefencesMonth;
	
	public User(String pseudo) {
		this.discordId = null;
		this.pseudo = pseudo;
		generalLadderPosition = -1;
		monthLadderPosition = -1;
		totalPoints = 0;
		monthPoints = 0;
		numberDefencesTotal = 0;
		numberDefencesMonth = 0;
	}

	public User(String discordId, String pseudo, int generalLadderPosition, int monthLadderPosition, float totalPoints, float monthPoints, int numberDefencesTotal, int numberDefencesMonth) {
		this.discordId = discordId;
		this.pseudo = pseudo;
		this.generalLadderPosition = generalLadderPosition;
		this.monthLadderPosition = monthLadderPosition;
		this.totalPoints = totalPoints;
		this.monthPoints = monthPoints;
		this.numberDefencesTotal =  numberDefencesTotal;
		this.numberDefencesMonth = numberDefencesMonth;
	}
	
	public String getDiscordId() { return discordId; }
	public void setDiscordId(String discordId) { this.discordId = discordId; }
	public String getPseudo() { return pseudo; }
	public void setPseudo(String pseudo) { this.pseudo = pseudo; }
	public int getGeneralLadderPosition() { return generalLadderPosition; }
	public int getMonthLadderPosition() { return monthLadderPosition; }
	public float getTotalPoints() { return totalPoints; }
	public float getMonthPoints() { return monthPoints; }
	public int getNumberDefencesTotal() { return numberDefencesTotal; }
	public int getNumberDefencesMonth() { return numberDefencesMonth; }
	public void setGeneralLadderPosition(int generalLadderPosition) { this.generalLadderPosition = generalLadderPosition; }
	public void setMonthLadderPosition(int monthLadderPosition) { this.monthLadderPosition = monthLadderPosition; }
	public void setTotalPoints(float totalPoints) { this.totalPoints = totalPoints; }
	public void setMonthPoints(float monthPoints) { this.monthPoints = monthPoints; }
	public void setNumberDefencesTotal(int numberDefencesTotal) { this.numberDefencesTotal = numberDefencesTotal; }
	public void setNumberDefencesMonth(int numberDefencesMonth) { this.numberDefencesMonth = numberDefencesMonth; }
	public void insert() throws SQLException { usersDAO.insertUser(this); }
	public void delete() throws SQLException { usersDAO.deleteUser(pseudo); }
	public void update() throws SQLException { usersDAO.updateUser(this); }
	public static void delete(String pseudo) throws SQLException { usersDAO.deleteUser(pseudo); }
	public static List<User> getAllUsers() throws SQLException { return usersDAO.selectAllUsers(); }
	public static List<User> getAllUsersByDiscordId(String discordId) throws SQLException { return usersDAO.selectUsersByDiscordId(discordId); }
	public static List<User> getAllUsersGroupByDiscordIdOrderBy(String columnName) throws SQLException { return usersDAO.selectUsersGroupByDiscordIdOrderBy(columnName); }
	public static List<User> getAllLinkedUsersGroupByDiscordId() throws SQLException { return usersDAO.selectAllLinkedUsersGroupByDiscordId(); }
	public static List<User> getAllLinkedUsersGroupByDiscordIdOrderBy(String columnName) throws SQLException { return usersDAO.selectLinkedUsersGroupByDiscordIdOrderBy(columnName); }
	public static List<User> getAllLinkedUsersGroupByDiscordIdSumTotalPointsdOrderByTotalPoints() throws SQLException { return usersDAO.selectLinkedUsersGroupByDiscordIdSumTotlaPointsdOrderByTotalPoints(); }
	public static List<User> getAllLinkedUsersGroupByDiscordIdSumMonthPointsdOrderByMonthPoints() throws SQLException { return usersDAO.selectLinkedUsersGroupByDiscordIdSumMonthPointsdOrderByMonthPoints(); }
	public static List<User> getAllLinkedUsers() throws SQLException { return usersDAO.selectAllLinkedUsers(); }
	public static List<User> getAllUnlinkedUsers() throws SQLException { return usersDAO.selectAllUnlinkedUsers(); }
	public static User getUserByPseudo(String pseudo) throws SQLException { return usersDAO.selectUserByPseudo(pseudo); }
	public static boolean isUserExist(String pseudo) throws SQLException { return usersDAO.isUserExist(pseudo); }
	public static boolean isLinked(String pseudo) throws SQLException { return usersDAO.isLinked(pseudo); }
}
