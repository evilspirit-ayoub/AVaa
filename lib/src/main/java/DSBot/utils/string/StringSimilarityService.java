package DSBot.utils.string;

import java.util.Comparator;
import java.util.List;

/**
 * A service that performs string similarity calculations.
 * @author Ralph Allan Rice <ralph.rice@gmail.com>
 *
 */
public interface StringSimilarityService {

	/**
     * Calculates all similarity scores for a given set of features.
     * @param features The list of features.
     * @param target The target string to compare against the features.
     * @return A list of similarity scores.
     */
    List<SimilarityScore> scoreAll(List<String> features, String target);
	
    /**
     * Calculates the similarity score of a single feature.
     * @param feature The feature string to compare.
     * @param target The target string to compare against the feature.
     * @return The similarity score between the feature and target.
     */
    double score(String feature, String target);

    
    /**
     * Finds the feature within a set of given features that best match the target string.
     * @param features A list of strings containing the features to compare.
     * @param target The target string to compare against the features.
     * @return A SimilarityScore that has the highest score value amongst the features.
     */
    SimilarityScore findTop(List<String> features, String target);

    /**
     * Finds the feature within a set of given features that best match the target string.
     * @param features A list of strings containing the features to compare.
     * @param target The target string to compare against the features.
     * @param comparator A comparator that is used sort the scores.  
     * @return A SimilarityScore that has the top value amongst the features, according to the comparator.
     */
    SimilarityScore findTop(List<String> features, String target, Comparator<SimilarityScore> comparator);
}
