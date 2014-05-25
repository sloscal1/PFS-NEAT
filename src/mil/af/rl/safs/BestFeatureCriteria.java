package mil.af.rl.safs;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.af.rl.predictive.SubspaceIdentification;
import mil.af.rl.util.DoubleIndexPair;
import mil.af.rl.util.DoubleIndexPair.FirstElementComparator;

import com.anji.util.Properties;

/**
 * This selection criteria will only add at most a single feature. It expects that all feature
 * scores of previously selected features will be set to negative numbers and that the allFeatures
 * list is sorted in descending order.
 * 
 * @author sloscal1
 *
 */
public class BestFeatureCriteria implements SelectionStrategy {

	@Override
	public void init(Properties arg0) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<Integer, Integer> selectFeatures(
			List<DoubleIndexPair<Integer, Double>> allFeatures) {
		//Find the first non-negative score from allFeatures:
		int i = 0;
		while(i != allFeatures.size() && allFeatures.get(i).getElement2() < 0.0)
			++i;
		int feature = (i != allFeatures.size())? allFeatures.get(i).getElement1() : -1;
		
		FirstElementComparator<Integer, Double> comp = new DoubleIndexPair.FirstElementComparator<Integer, Double>();
		Collections.sort(allFeatures, comp);
		Map<Integer, Integer> map = new HashMap<>();
		for(int j = 0, next = 0; j < allFeatures.size(); ++j)
			if(allFeatures.get(j).getElement2() >= 0.0){
				if(j != feature)
					map.put(j, SubspaceIdentification.UNUSED);
				else
					map.put(j, next++);
			}
			else
				map.put(j, next++);
		return map;
	}

	@Override
	public void reset() {
		//No state to reset
	}

}
