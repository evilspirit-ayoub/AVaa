package DSBot.database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import DSBot.database.model.Ladder;

public class LadderDAO extends DAO {
	
	private static final String INSERT_LADDER = "INSERT INTO Ladders (pseudos, positions, points, date) VALUES (?, ?, ?, ?);";
	private static final String SELECT_LADDER_BY_ID = "select * from Ladders where id = ?;";
	private static final String SELECT_LADDER_BY_DATE = "select * from Ladders where date = ?;";
	private static final String SELECT_ALL_LADDERS = "select * from Ladders;";
	private static final String DELETE_LADDER_BY_DATE = "delete from Ladders where date = ?;";
	private static final String UPDATE_LADDER_BY_DATE = "update Ladders set pseudos = ?, positions = ?, points = ? where date = ?;";;
	
	public LadderDAO(String driverClass, String jdbcURL, String jdbcUsername, String jdbcPassword) {
		super(driverClass, jdbcURL, jdbcUsername, jdbcPassword);
	}
	
	public void insertLadder(Ladder ladder) throws SQLException {
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(INSERT_LADDER)) {
			preparedStatement.setString(1, ladder.getDiscordIds().stream().collect(Collectors.joining(",")));
			preparedStatement.setString(2, ladder.getPositions().stream().map(position -> String.valueOf(position)).collect(Collectors.joining(",")));
			preparedStatement.setString(3, ladder.getPoints().stream().map(points -> String.valueOf(points)).collect(Collectors.joining(",")));
			preparedStatement.setString(4, ladder.getDate());
			preparedStatement.executeUpdate();
		} catch (SQLException e) { printSQLException(e); }
	}
	
	public Ladder selectLadderBydId(String id) {
		Ladder ladder = null;
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_LADDER_BY_ID);) {
			preparedStatement.setString(1, id);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				String pseudos = rs.getString("pseudos");
				String positions = rs.getString("positions");
				String points = rs.getString("points");
				String date = rs.getString("date");
				ladder = new Ladder(Arrays.asList(pseudos.split(",")),
						Arrays.asList(positions.split(",")).stream().map(str -> Integer.valueOf(str)).toList(),
						Arrays.asList(points.split(",")).stream().map(str -> Float.valueOf(str)).toList(),
						date);
			}
		} catch (SQLException e) { printSQLException(e); }
		return ladder;
	}
	
	public Ladder selectLadderByDate(String date) {
		Ladder ladder = null;
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_LADDER_BY_DATE);) {
			preparedStatement.setString(1, date);
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				String pseudos = rs.getString("pseudos");
				String positions = rs.getString("positions");
				String points = rs.getString("points");
				ladder = new Ladder(Arrays.asList(pseudos.split(",")),
						Arrays.asList(positions.split(",")).stream().map(str -> Integer.valueOf(str)).toList(),
						Arrays.asList(points.split(",")).stream().map(str -> Float.valueOf(str)).toList(),
						date);
			}
		} catch (SQLException e) { printSQLException(e); }
		return ladder;
	}
	
	public List<Ladder> selectAllLadders() {
		List<Ladder> ladders = new ArrayList<>();
		try (Connection connection = getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_LADDERS);) {
			ResultSet rs = preparedStatement.executeQuery();
			while (rs.next()) {
				String pseudos = rs.getString("pseudos");
				String positions = rs.getString("positions");
				String points = rs.getString("points");
				String date = rs.getString("date");
				ladders.add(new Ladder(Arrays.asList(pseudos.split(",")),
						Arrays.asList(positions.split(",")).stream().map(str -> Integer.valueOf(str)).toList(),
						Arrays.asList(points.split(",")).stream().map(str -> Float.valueOf(str)).toList(),
						date));
			}
		} catch (SQLException e) { printSQLException(e); }
		return ladders;
	}
	
	public boolean deleteLadder(String date) throws SQLException {
		boolean rowDeleted;
		try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(DELETE_LADDER_BY_DATE);) {
			statement.setString(1, date);
			rowDeleted = statement.executeUpdate() > 0;
		}
		return rowDeleted;
	}
	
	public boolean updateLadder(Ladder ladder) throws SQLException {
		boolean rowUpdated;
		try (Connection connection = getConnection(); PreparedStatement statement = connection.prepareStatement(UPDATE_LADDER_BY_DATE);) {
			statement.setString(1, ladder.getDiscordIds().stream().collect(Collectors.joining(",")));
			statement.setString(2, ladder.getPositions().stream().map(pos -> String.valueOf(pos)).collect(Collectors.joining(",")));
			statement.setString(3, ladder.getPoints().stream().map(pts -> String.valueOf(pts)).collect(Collectors.joining(",")));
			statement.setString(4, ladder.getDate());
			rowUpdated = statement.executeUpdate() > 0;
		}
		return rowUpdated;
	}
}
