package Ladder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {
	
//
	private static final long serialVersionUID = 1L;
	private String pseudo;
	private String id;
	private int generalLadderPosition;
	private int monthLadderPosition;
	private int totalPoints;
	private int monthPoints;
	private int numberAttacksTotal;
	private int numberDefencesTotal;
	private int numberAttacksMonth;
	private int numberDefencesMonth;
	
	public Player(String pseudo) {
		this.id = "NO ID";
		this.pseudo = pseudo;
		generalLadderPosition = -1;
		monthLadderPosition = -1;
		totalPoints = 0;
		monthPoints = 0;
		numberAttacksTotal = 0;
		numberDefencesTotal = 0;
		numberAttacksMonth = 0;
		numberDefencesMonth = 0;
	}
	
	public Player(String id, String pseudo) {
		this.id = id;
		this.pseudo = pseudo;
		generalLadderPosition = -1;
		monthLadderPosition = -1;
		totalPoints = 0;
		monthPoints = 0;
		numberAttacksTotal = 0;
		numberDefencesTotal = 0;
		numberAttacksMonth = 0;
		numberDefencesMonth = 0;
	}
	
	public String getId() { return id; }
	public String getPseudo() { return pseudo; }
	public int getGeneralLadderPosition() { return generalLadderPosition; }
	public int getMonthLadderPosition() { return monthLadderPosition; }
	public int getTotalPoints() { return totalPoints; }
	public int getMonthPoints() { return monthPoints; }
	public int getNumberAttacksTotal() { return numberAttacksTotal; }
	public int getNumberDefencesTotal() { return numberDefencesTotal; }
	public int getNumberAttacksMonth() { return numberAttacksMonth; }
	public int getNumberDefencesMonth() { return numberDefencesMonth; }
	public void setId(String id) { this.id = id; }
	public void setGeneralLadderPosition(int generalLadderPosition) { this.generalLadderPosition = generalLadderPosition; }
	public void setMonthLadderPosition(int monthLadderPosition) { this.monthLadderPosition = monthLadderPosition; }
	public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
	public void setMonthPoints(int monthPoints) { this.monthPoints = monthPoints; }
	public void setNumberAttacksTotal(int numberAttacksTotal) { this.numberAttacksTotal = numberAttacksTotal; }
	public void setNumberDefencesTotal(int numberDefencesTotal) { this.numberDefencesTotal = numberDefencesTotal; }
	public void setNumberAttacksMonth(int numberAttacksMonth) { this.numberAttacksMonth = numberAttacksMonth; }
	public void setNumberDefencesMonth(int numberDefencesMonth) { this.numberDefencesMonth = numberDefencesMonth; }
	
	public String toSring() { return "[Pseudo : " + pseudo
			+ ", id : " + id 
			+ ", total point(s) : " + totalPoints
			+ ", month point(s) : " + monthPoints
			+ ", attack(s) total : " + numberAttacksTotal
			+ ", defense(s) total : " + numberDefencesTotal
			+ ", attack(s) month : " + numberAttacksMonth
			+ ", defense(s) month : " + numberDefencesMonth + "]";
	}
	
	public static void serialize(String guild, List<Player> players) throws IOException {
        try {
        	File data = new File(guild + "/data");
        	if(!data.exists()) data.createNewFile();
    		FileOutputStream fos = new FileOutputStream(data);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(players);
            oos.close();
            fos.close();
        } catch (Exception e) { e.printStackTrace(); }
	}
	
	public static List<Player> unserialize(String guild) throws IOException, ClassNotFoundException, InterruptedException {
		File data = new File(guild + "/data");
		if(!data.exists()) return new ArrayList<>();
		FileInputStream fis = new FileInputStream(data);
        ObjectInputStream ois = new ObjectInputStream(fis);
        List<Player> player = (ArrayList) ois.readObject();
        ois.close();
        fis.close();
        return player;
	}
}
