package DSBot.utils.string;

/**
 * @author Ralph Allan Rice <ralph.rice@gmail.com>
 * An interface that defines methods to perform string similarity calculation.
 */
public interface SimilarityStrategy {

	/**
	 * Calculates the similarity score of objects, where 0.0 implies absolutely no similarity
	 * and 1.0 implies absolute similarity.
	 * 
	 * @param first The first string to compare.
	 * @param second The second string to compare.
	 * @return A number between 0.0 and 1.0.
	 */
    double score(String first, String second);
}
