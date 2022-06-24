package DSBot.utils.string;

/**
 * A value object contains a similarity score.
 * @author Ralph Allan Rice <ralph.rice@gmail.com>
 * 
 */
public class SimilarityScore {
	
	private String key;
	private double score;
	
	/**
	 * Constructs a similarity score.
	 * @param key The string key.
	 * @param score The score value.
	 */

	public SimilarityScore(String key, double score) {
		this.key = key;
		this.score = score;
	}
	
	/**
	 * Gets the key for this score.
	 * @return A string.
	 */
	public String getKey() {
		return this.key;
	}
	
	/**
	 * Gets the value of the score.
	 * @return A double.
	 */
	public double getScore() {
		return this.score;
	}

	
	/**
	 * Returns the hash code for this object.
	 * @return An integer representing the hash code.
	 */
	public int hashCode() {
		int hash = 11;
		hash = 23 * hash + key.hashCode(); 
		hash = 23 * hash + (int)(score * 1000000);
		return hash;
	}
	
	/**
	 * Determines if the supplied object equals this object.
	 * @return True if the keys and scores match between the two objects. Otherwise false.
	 */
	@Override
	public boolean equals(Object o) {
		if((o == null) || (o.getClass() != this.getClass())) {
			return false; 
		}
		SimilarityScore other=(SimilarityScore)o;
		
		return this.key.equals(other.key)
					&& this.score == other.score;
	}
	
	
}
