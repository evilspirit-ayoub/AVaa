package DSBot.utils.string;

import java.util.Comparator;

/**
 * A comparator that allows SimilarityScore to be sorted in
 * ascending order.
 * @author Ralph Allan Rice <ralph.rice@gmail.com>
 *
 */
public class AscendingSimilarityScoreComparator implements Comparator<SimilarityScore>
{
	/**
	 * Compares two similarity scores.
	 * @param x The first score to be compared.
	 * @param y The second score to be compared.
	 * @return a negative integer, zero, or a positive integer as the first score is less than,
	 * equal to, or greater than the second score. 
	 */
	public int compare(SimilarityScore x, SimilarityScore y) {
		double first = x.getScore();
		double second = y.getScore();
		if (first == second) {
			return 0;
		}
		if (first < second) {
			return -1;
		}
		return 1;
	}
	
}
