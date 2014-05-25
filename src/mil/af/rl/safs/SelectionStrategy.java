package mil.af.rl.safs;

import java.util.List;
import java.util.Map;

import mil.af.rl.util.DoubleIndexPair;

import com.anji.util.Configurable;

/**
 * This interface exists to help experiment with different cut strategies for
 * the ranking feature selection algorithms used at this time in the progressive
 * framework.
 * 
 * @author sloscal1
 *
 */
public interface SelectionStrategy extends Configurable{

	/**
	 * Selected features based on the rankings given by allFeatures.  Each pair
	 * is a feature index and its score from whatever the evaluation method was.
	 * 
	 * It is assumed that the allFeatures list is sorted in descending order and
	 * that the most relevant features have the highest scores (come first).
	 * 
	 * @param allFeatures
	 * @return A mapping of all features, where each index (key) maps to its selection
	 * rank (value).  If its value is -1 it means that the index is not selected
	 * (UNUSED).
	 */
	Map<Integer, Integer> selectFeatures(List<DoubleIndexPair<Integer, Double>> allFeatures);
	
	/**
	 * If there is any memory associated with this selection strategy, this method
	 * will clear it out.
	 */
	void reset();
}
