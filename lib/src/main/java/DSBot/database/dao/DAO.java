package DSBot.database.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DAO {

	private String driverClass;
	private String jdbcURL;
	private String jdbcUsername;
	private String jdbcPassword;
	
	public DAO(String driverClass, String jdbcURL, String jdbcUsername, String jdbcPassword) {
		this.driverClass = driverClass;
		this.jdbcURL = jdbcURL;
		this.jdbcUsername = jdbcUsername;
		this.jdbcPassword = jdbcPassword;
	}
	
	protected Connection getConnection() {
		Connection connection = null;
		try {
			Class.forName(driverClass);
			connection = DriverManager.getConnection(jdbcURL, jdbcUsername, jdbcPassword);
		} catch (SQLException | ClassNotFoundException e) { e.printStackTrace(); }
		return connection;
	}
	
	protected void printSQLException(SQLException ex) {
		for (Throwable e : ex) {
			if (e instanceof SQLException) {
				e.printStackTrace(System.err);
				System.err.println("SQLState: " + ((SQLException) e).getSQLState());
				System.err.println("Error Code: " + ((SQLException) e).getErrorCode());
				System.err.println("Message: " + e.getMessage());
				Throwable t = ex.getCause();
				while (t != null) {
					System.out.println("Cause: " + t);
					t = t.getCause();
				}
			}
		}
	}
}
