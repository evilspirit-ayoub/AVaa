package DSBot.database.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import DSBot.database.model.Screen;

public class ScreenDAO extends DAO {
	
	private static final String INSERT_SCREEN = "INSERT INTO Screens (messageId, pseudo, isVictory, versus, points, date) VALUES (?, ?, ?, ?, ?, ?);";
	private static final String SELECT_SCREEN_BY_ID = "select * from Screens where messageId =?";
	private static final String SELECT_ALL_SCREENS = "select * from Screens";
	private static final String DELETE_SCREEN_BY_ID = "delete from Screens where messageId = ?;";
	private static final String UPDATE_SCREEN_BY_ID = "update Screens set messageId = ?, pseudo = ?, isVictory= ?, versus =?, points =?, date =? where messageId = ?;";
	private static final String SELECT_LAST_SCREEN = "select * from Screens order by id desc limit 1;";
	
	public ScreenDAO(String driverClass, String jdbcURL, String jdbcUsername, String jdbcPassword) {
		super(driverClass, jdbcURL, jdbcUsername, jdbcPassword);
	}
	
	public void insertScreen(Screen screen) throws SQLException {
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SCREEN)) {
			preparedStatement.setString(1, screen.getmessageId());
			preparedStatement.setString(2, screen.getPseudos().stream().collect(Collectors.joining(",")));
			preparedStatement.setBoolean(3, screen.isVictory());
			preparedStatement.setString(4, screen.getVersus());
			preparedStatement.setFloat(5, screen.getPoints());
			preparedStatement.setDate(6, Date.valueOf(screen.getDate()));
			preparedStatement.executeUpdate();
		} catch (SQLException e) { printSQLException(e); }
	}
	
	public Screen selectScreen(String id) {
		Screen screen = null;
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SCREEN_BY_ID);) {
			preparedStatement.setString(1, id);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				String pseudo = rs.getString("pseudo");
				boolean isVictory = rs.getBoolean("isVictory");
				String versus = rs.getString("versus");
				float points = rs.getFloat("points");
				Date date = rs.getDate("date");
				screen = new Screen(id, Arrays.asList(pseudo.split(",")), isVictory, versus, points, date.toLocalDate());
			}
		} catch (SQLException e) { printSQLException(e); }
		return screen;
	}
	
	public List<Screen> selectAllScreens() {
		List<Screen> screens = new ArrayList<>();
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_SCREENS);) {
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				String messageId = rs.getString("messageId");
				String pseudo = rs.getString("pseudo");
				boolean isVictory = rs.getBoolean("isVictory");
				String versus = rs.getString("versus");
				float points = rs.getFloat("points");
				Date date = rs.getDate("date");
				screens.add(new Screen(messageId, Arrays.asList(pseudo.split(",")), isVictory, versus, points, date.toLocalDate()));
			}
		} catch (SQLException e) { printSQLException(e); }
		return screens;
	}
	
	public boolean deleteScreen(String messageId) throws SQLException {
		boolean rowDeleted;
		try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(DELETE_SCREEN_BY_ID);) {
			statement.setString(1, messageId);
			rowDeleted = statement.executeUpdate() > 0;
		}
		return rowDeleted;
	}
	
	public boolean updateScreen(Screen screen) throws SQLException {
		boolean rowUpdated;
		try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(UPDATE_SCREEN_BY_ID);) {
			statement.setString(1, screen.getmessageId());
			statement.setString(2, screen.getPseudos().stream().collect(Collectors.joining(",")));
			statement.setBoolean(3, screen.isVictory());
			statement.setString(4, screen.getVersus());
			statement.setFloat(5, screen.getPoints());
			statement.setDate(6, Date.valueOf(screen.getDate()));
			statement.setString(7, screen.getmessageId());
			rowUpdated = statement.executeUpdate() > 0;
		}
		return rowUpdated;
	}
	
	public Screen getLastScreen() {
		Screen screen = null;
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_LAST_SCREEN);) {
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				String messageId = rs.getString("messageId");
				String pseudo = rs.getString("pseudo");
				boolean isVictory = rs.getBoolean("isVictory");
				String versus = rs.getString("versus");
				float points = rs.getFloat("points");
				Date date = rs.getDate("date");
				screen = new Screen(messageId, Arrays.asList(pseudo.split(",")), isVictory, versus, points, date.toLocalDate());
			}
		} catch (SQLException e) { printSQLException(e); }
		return screen;
	}
}
