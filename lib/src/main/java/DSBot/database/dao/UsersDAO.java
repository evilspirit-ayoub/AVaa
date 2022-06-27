package DSBot.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import DSBot.database.model.User;

public class UsersDAO extends DAO {
	
	private static final String INSERT_USER = "INSERT INTO Users (discordId, pseudo, generalLadderPosition, monthLadderPosition, totalPoints, monthPoints, numberDefencesTotal, numberDefencesMonth) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
	private static final String SELECT_USER_BY_PSEUDO = "select * from Users where pseudo =?";
	private static final String SELECT_USERS_BY_DISCORD_ID = "select * from Users where discordId =?";
	private static final String SELECT_LINKED_USERS_GROUP_BY_DISCORD_ID = "select * from Users where discordId is not null group by discordId;";
	private static final String SELECT_LINKED_USERS_GROUP_BY_DISCORD_ID_ORDER_BY = "select * from Users where discordId is not null group by discordId order by ? desc;";
	private static final String SELECT_LINKED_USERS_GROUP_BY_DISCORD_ID_SUM_TOTAL_POINTS_ORDER_BY_TOTAL_POINTS = "select *,sum(totalPoints) as tp from Users where discordId is not null group by discordId order by tp desc;";
	private static final String SELECT_LINKED_USERS_GROUP_BY_DISCORD_ID_SUM_MONTH_POINTS_ORDER_BY_MONTH_POINTS = "select *,sum(monthPoints) as mp from Users where discordId is not null group by discordId order by mp desc;";
	private static final String SELECT_USERS_GROUP_BY_ID_ORDER_BY_POSITION = "select * from Users group by discordId order by ? desc;";
	private static final String SELECT_ALL_USERS = "select * from Users";
	private static final String DELETE_USER_BY_PSEUDO = "delete from Users where pseudo = ?;";
	private static final String UPDATE_USER_BY_PSEUDO = "update Users set discordId = ?, pseudo = ?, generalLadderPosition =?, monthLadderPosition=?, totalPoints =?, monthPoints =?, numberDefencesTotal =?, numberDefencesMonth = ? where pseudo = ?;";
	private static final String SELECT_ALL_UNLINKED_USERS = "select * from Users where discordId is null";
	private static final String SELECT_ALL_LINKED_USERS = "select * from Users where discordId is not null";

	public UsersDAO(String driverClass, String jdbcURL, String jdbcUsername, String jdbcPassword) {
		super(driverClass, jdbcURL, jdbcUsername, jdbcPassword);
	}

	public void insertUser(User user) throws SQLException {
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(INSERT_USER)) {
			preparedStatement.setString(1, user.getDiscordId());
			preparedStatement.setString(2, user.getPseudo());
			preparedStatement.setInt(3, user.getGeneralLadderPosition());
			preparedStatement.setInt(4, user.getMonthLadderPosition());
			preparedStatement.setFloat(5, user.getTotalPoints());
			preparedStatement.setFloat(6, user.getMonthPoints());
			preparedStatement.setInt(7, user.getNumberDefencesTotal());
			preparedStatement.setInt(8, user.getNumberDefencesMonth());
			preparedStatement.executeUpdate();
		} catch (SQLException e) { printSQLException(e); }
	}

	public List<User> selectUsersByDiscordId(String discordId) {
		List<User> users = new ArrayList<>();
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USERS_BY_DISCORD_ID);) {
			preparedStatement.setString(1, discordId);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				String pseudo = rs.getString("pseudo");
				int generalLadderPosition = rs.getInt("generalLadderPosition");
				int monthLadderPosition = rs.getInt("monthLadderPosition");
				float totalPoints = rs.getFloat("totalPoints");
				float monthPoints = rs.getFloat("monthPoints");
				int numberDefencesTotal = rs.getInt("numberDefencesTotal");
				int numberDefencesMonth = rs.getInt("numberDefencesMonth");
				users.add(new User(discordId, pseudo, generalLadderPosition, monthLadderPosition, totalPoints, monthPoints, numberDefencesTotal, numberDefencesMonth));
			}
		} catch (SQLException e) { printSQLException(e); }
		return users;
	}
	
	public User selectUserByPseudo(String pseudo) {
		User user = null;
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USER_BY_PSEUDO);) {
			preparedStatement.setString(1, pseudo);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				String discordId = rs.getString("discordId");
				int generalLadderPosition = rs.getInt("generalLadderPosition");
				int monthLadderPosition = rs.getInt("monthLadderPosition");
				float totalPoints = rs.getFloat("totalPoints");
				float monthPoints = rs.getFloat("monthPoints");
				int numberDefencesTotal = rs.getInt("numberDefencesTotal");
				int numberDefencesMonth = rs.getInt("numberDefencesMonth");
				user = new User(discordId, pseudo, generalLadderPosition, monthLadderPosition, totalPoints, monthPoints, numberDefencesTotal, numberDefencesMonth);
			}
		} catch (SQLException e) { printSQLException(e); }
		return user;
	}

	public List<User> selectAllUsers() {
		List<User> users = new ArrayList<>();
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_USERS);) {
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				String discordId = rs.getString("discordId");
				String pseudo = rs.getString("pseudo");
				int generalLadderPosition = rs.getInt("generalLadderPosition");
				int monthLadderPosition = rs.getInt("monthLadderPosition");
				float totalPoints = rs.getFloat("totalPoints");
				float monthPoints = rs.getFloat("monthPoints");
				int numberDefencesTotal = rs.getInt("numberDefencesTotal");
				int numberDefencesMonth = rs.getInt("numberDefencesMonth");
				users.add(new User(discordId, pseudo, generalLadderPosition, monthLadderPosition, totalPoints, monthPoints, numberDefencesTotal, numberDefencesMonth));
			}
		} catch (SQLException e) { printSQLException(e); }
		return users;
	}
	
	public boolean deleteUser(String pseudo) throws SQLException {
		boolean rowDeleted;
		try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(DELETE_USER_BY_PSEUDO);) {
			statement.setString(1, pseudo);
			rowDeleted = statement.executeUpdate() > 0;
		}
		return rowDeleted;
	}

	public boolean updateUser(User user) throws SQLException {
		boolean rowUpdated;
		try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(UPDATE_USER_BY_PSEUDO);) {
			statement.setString(1, user.getDiscordId());
			statement.setString(2, user.getPseudo());
			statement.setInt(3, user.getGeneralLadderPosition());
			statement.setInt(4, user.getMonthLadderPosition());
			statement.setFloat(5, user.getTotalPoints());
			statement.setFloat(6, user.getMonthPoints());
			statement.setInt(7, user.getNumberDefencesTotal());
			statement.setInt(8, user.getNumberDefencesMonth());
			statement.setString(9, user.getPseudo());
			rowUpdated = statement.executeUpdate() > 0;
		}
		return rowUpdated;
	}
	
	public List<User> selectAllUnlinkedUsers() {
		List<User> users = new ArrayList<>();
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_UNLINKED_USERS);) {
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				String discordId = rs.getString("discordId");
				String pseudo = rs.getString("pseudo");
				int generalLadderPosition = rs.getInt("generalLadderPosition");
				int monthLadderPosition = rs.getInt("monthLadderPosition");
				float totalPoints = rs.getFloat("totalPoints");
				float monthPoints = rs.getFloat("monthPoints");
				int numberDefencesTotal = rs.getInt("numberDefencesTotal");
				int numberDefencesMonth = rs.getInt("numberDefencesMonth");
				users.add(new User(discordId, pseudo, generalLadderPosition, monthLadderPosition, totalPoints, monthPoints, numberDefencesTotal, numberDefencesMonth));
			}
		} catch (SQLException e) { printSQLException(e); }
		return users;
	}
	
	public List<User> selectAllLinkedUsers() {
		List<User> users = new ArrayList<>();
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_LINKED_USERS);) {
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				String discordId = rs.getString("discordId");
				String pseudo = rs.getString("pseudo");
				int generalLadderPosition = rs.getInt("generalLadderPosition");
				int monthLadderPosition = rs.getInt("monthLadderPosition");
				float totalPoints = rs.getFloat("totalPoints");
				float monthPoints = rs.getFloat("monthPoints");
				int numberDefencesTotal = rs.getInt("numberDefencesTotal");
				int numberDefencesMonth = rs.getInt("numberDefencesMonth");
				users.add(new User(discordId, pseudo, generalLadderPosition, monthLadderPosition, totalPoints, monthPoints, numberDefencesTotal, numberDefencesMonth));
			}
		} catch (SQLException e) { printSQLException(e); }
		return users;
	}
	
	public boolean isUserExist(String pseudo) {
		User user = selectUserByPseudo(pseudo);
		return user != null;
	}
	
	public boolean isLinked(String pseudo) {
		User user = selectUserByPseudo(pseudo);
		if(user == null) return false;
		return user.getDiscordId() != null;
	}

	public List<User> selectLinkedUsersGroupByDiscordIdOrderBy(String columnName) {
		List<User> users = new ArrayList<>();
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_LINKED_USERS_GROUP_BY_DISCORD_ID_ORDER_BY);) {
			preparedStatement.setString(1, columnName);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				String discordId = rs.getString("discordId");
				String pseudo = rs.getString("pseudo");
				int generalLadderPosition = rs.getInt("generalLadderPosition");
				int monthLadderPosition = rs.getInt("monthLadderPosition");
				float totalPoints = rs.getFloat("totalPoints");
				float monthPoints = rs.getFloat("monthPoints");
				int numberDefencesTotal = rs.getInt("numberDefencesTotal");
				int numberDefencesMonth = rs.getInt("numberDefencesMonth");
				users.add(new User(discordId, pseudo, generalLadderPosition, monthLadderPosition, totalPoints, monthPoints, numberDefencesTotal, numberDefencesMonth));
			}
		} catch (SQLException e) { printSQLException(e); }
		return users;
	}
	
	public List<User> selectLinkedUsersGroupByDiscordIdSumTotlaPointsdOrderByTotalPoints() {
		List<User> users = new ArrayList<>();
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_LINKED_USERS_GROUP_BY_DISCORD_ID_SUM_TOTAL_POINTS_ORDER_BY_TOTAL_POINTS);) {
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				String discordId = rs.getString("discordId");
				String pseudo = rs.getString("pseudo");
				int generalLadderPosition = rs.getInt("generalLadderPosition");
				int monthLadderPosition = rs.getInt("monthLadderPosition");
				float totalPoints = rs.getFloat("tp");
				float monthPoints = rs.getFloat("monthPoints");
				int numberDefencesTotal = rs.getInt("numberDefencesTotal");
				int numberDefencesMonth = rs.getInt("numberDefencesMonth");
				users.add(new User(discordId, pseudo, generalLadderPosition, monthLadderPosition, totalPoints, monthPoints, numberDefencesTotal, numberDefencesMonth));
			}
		} catch (SQLException e) { printSQLException(e); }
		return users;
	}
	
	public List<User> selectLinkedUsersGroupByDiscordIdSumMonthPointsdOrderByMonthPoints() {
		List<User> users = new ArrayList<>();
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_LINKED_USERS_GROUP_BY_DISCORD_ID_SUM_MONTH_POINTS_ORDER_BY_MONTH_POINTS);) {
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				String discordId = rs.getString("discordId");
				String pseudo = rs.getString("pseudo");
				int generalLadderPosition = rs.getInt("generalLadderPosition");
				int monthLadderPosition = rs.getInt("monthLadderPosition");
				float totalPoints = rs.getFloat("totalPoints");
				float monthPoints = rs.getFloat("mp");
				int numberDefencesTotal = rs.getInt("numberDefencesTotal");
				int numberDefencesMonth = rs.getInt("numberDefencesMonth");
				users.add(new User(discordId, pseudo, generalLadderPosition, monthLadderPosition, totalPoints, monthPoints, numberDefencesTotal, numberDefencesMonth));
			}
		} catch (SQLException e) { printSQLException(e); }
		return users;
	}

	public List<User> selectUsersGroupByDiscordIdOrderBy(String columnName) {
		List<User> users = new ArrayList<>();
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_USERS_GROUP_BY_ID_ORDER_BY_POSITION);) {
			preparedStatement.setString(1, columnName);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				String discordId = rs.getString("discordId");
				String pseudo = rs.getString("pseudo");
				int generalLadderPosition = rs.getInt("generalLadderPosition");
				int monthLadderPosition = rs.getInt("monthLadderPosition");
				float totalPoints = rs.getFloat("totalPoints");
				float monthPoints = rs.getFloat("monthPoints");
				int numberDefencesTotal = rs.getInt("numberDefencesTotal");
				int numberDefencesMonth = rs.getInt("numberDefencesMonth");
				users.add(new User(discordId, pseudo, generalLadderPosition, monthLadderPosition, totalPoints, monthPoints, numberDefencesTotal, numberDefencesMonth));
			}
		} catch (SQLException e) { printSQLException(e); }
		return users;
	}
	
	public List<User> selectAllLinkedUsersGroupByDiscordId() {
		List<User> users = new ArrayList<>();
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_LINKED_USERS_GROUP_BY_DISCORD_ID);) {
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				String discordId = rs.getString("discordId");
				String pseudo = rs.getString("pseudo");
				int generalLadderPosition = rs.getInt("generalLadderPosition");
				int monthLadderPosition = rs.getInt("monthLadderPosition");
				float totalPoints = rs.getFloat("totalPoints");
				float monthPoints = rs.getFloat("monthPoints");
				int numberDefencesTotal = rs.getInt("numberDefencesTotal");
				int numberDefencesMonth = rs.getInt("numberDefencesMonth");
				users.add(new User(discordId, pseudo, generalLadderPosition, monthLadderPosition, totalPoints, monthPoints, numberDefencesTotal, numberDefencesMonth));
			}
		} catch (SQLException e) { printSQLException(e); }
		return users;
	}
}
