package DSBot.database.model;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

import DSBot.database.dao.ScreenDAO;

public class Screen {
	
	public final static ScreenDAO screenDAO;
	static {
		final String PATH_FILE_PROPERTIES = "conf.properties";
		Properties properties = new Properties();
		try (FileInputStream fis = new FileInputStream(PATH_FILE_PROPERTIES);) { properties.load(fis);}
		catch (IOException e) { e.printStackTrace(); }
		screenDAO = new ScreenDAO(properties.getProperty("jdbc.driver.class"), properties.getProperty("jdbc.url"), properties.getProperty("jdbc.username"), properties.getProperty("jdbc.password"));
	}
	
	private String messageId, versus;
	private List<String> pseudos;
	private boolean isVictory;
	double points;
	private LocalDate date;
	
	public Screen(String messageId, List<String> pseudos, boolean isVictory, String versus, double points, LocalDate date) {
		this.messageId = messageId;
		this.pseudos = pseudos;
		this.isVictory = isVictory;
		this.versus = versus;
		this.points = points;
		this.date = date;
	}
	
	public String getmessageId() { return messageId; }
	public void setmessageId(String messageId) { this.messageId = messageId; }
	public List<String> getPseudos() { return pseudos; }
	public void setPseudos(List<String> pseudos) { this.pseudos = pseudos; }
	public boolean isVictory() { return isVictory; }
	public void setVictory(boolean isVictory) { this.isVictory = isVictory; }
	public String getVersus() { return versus; }
	public void setVersus(String versus) { this.versus = versus; }
	public double getPoints() { return points; }
	public void setPoints(double points) { this.points = points; }
	public LocalDate getDate() { return date; }
	public void setDate(LocalDate date) { this.date = date; }
	public void insert() throws SQLException { screenDAO.insertScreen(this); }
	public void delete() throws SQLException { screenDAO.deleteScreen(messageId); }
	public void update() throws SQLException { screenDAO.updateScreen(this); }
	public static void delete(String messageId) throws SQLException { screenDAO.deleteScreen(messageId); }
	public static Screen getScreen(String messageId) throws SQLException { return screenDAO.selectScreen(messageId); }
	public static List<Screen> getAllScreens() throws SQLException { return screenDAO.selectAllScreens(); }
	public static Screen getLastScreen() throws SQLException { return screenDAO.getLastScreen(); }
}
