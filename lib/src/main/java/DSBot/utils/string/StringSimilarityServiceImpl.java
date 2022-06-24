package DSBot.utils.string;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Collections;


/**
 * An implementation of StringSimilarityService.
 * @author Ralph Allan Rice <ralph.rice@gmail.com>
 * @see StringSimilarityService
 */
public class StringSimilarityServiceImpl implements StringSimilarityService {

	private SimilarityStrategy strategy;


    /**
     * Creates a similarity calculator instance.
     * @param strategy The similarity strategy to use when calculating similarity scores.
     */
    public StringSimilarityServiceImpl(SimilarityStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Calculates all similarity scores for a given set of features.
     * @param features The list of features.
     * @param target The target string to compare against the features.
     * @return A list of similarity scores.
     */
    public List<SimilarityScore> scoreAll(List<String> features, String target)
    {
        ArrayList<SimilarityScore> scores = new ArrayList<SimilarityScore>();
        
        for(String feature: features) {
        	double score = strategy.score(feature, target);
        	scores.add(new SimilarityScore(feature, score));
        }
        
        return scores;
    }


    /**
     * Calculates the similarity score of a single feature.
     * @param feature The feature string to compare.
     * @param target The target string to compare against the feature.
     * @return The similarity score between the feature and target.
     */
    public double score(String feature, String target)
    {
        return strategy.score(feature, target);
    }

    /**
     * Finds the feature within a set of given features that best match the target string.
     * @param features A list of strings containing the features to compare.
     * @param target The target string to compare against the features.
     * @return The similarity score with the highest value.
     */
    public SimilarityScore findTop(List<String> features, String target)
    {
    	return findTop(features, target, new DescendingSimilarityScoreComparator());
    }
    
    /**
     * Finds the feature within a set of given features that best match the target string.
     * @param features A list of strings containing the features to compare.
     * @param target The target string to compare against the features.
     * @param comparator A comparator that is used sort the scores.  
     * @return A SimilarityScore that has the top value amongst the features, according to the comparator.
     */
    public SimilarityScore findTop(List<String> features, String target, Comparator<SimilarityScore> comparator)
    {
    	if (features.size() == 0) {
    		return null;
    	}
    	List<SimilarityScore> scores= scoreAll(features, target);
    	Collections.sort(scores, comparator);
    	return scores.get(0);
    }
}
