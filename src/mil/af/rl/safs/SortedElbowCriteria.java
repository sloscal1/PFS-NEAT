package mil.af.rl.safs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import mil.af.rl.predictive.SubspaceIdentification;
import mil.af.rl.util.DoubleIndexPair;

import com.anji.util.Properties;

public class SortedElbowCriteria implements SelectionStrategy {
	
	/**
	 * Default constructor needed for Configurable.
	 */
	public SortedElbowCriteria(){}
	
	@Override
	public void reset() {
		//No state associated with this selection strategy
	}

	@Override
	public Map<Integer, Integer> selectFeatures(List<DoubleIndexPair<Integer, Double>> allFeatures) {
		//The return value.
		Map<Integer, Integer> mapping = new LinkedHashMap<Integer, Integer>();

		//first, want to sort the differences
		ArrayList<DoubleIndexPair<Integer, Double>> elbows = new ArrayList<DoubleIndexPair<Integer,Double>>();
		for(int i = 0; i < allFeatures.size()-1; ++i){
			double val = allFeatures.get(i).getElement2() - allFeatures.get(i+1).getElement2();
			elbows.add(new DoubleIndexPair<Integer, Double>(i, val));
		}	
		Collections.sort(elbows);
		Collections.reverse(elbows);
		
		//second, find biggest difference of differences to get elbow point
		int elbowL2 = allFeatures.size() - 1;
		double l2Diff = 0;
		for(int i = 0; i < elbows.size()-1; i++){
			double val = elbows.get(i).getElement2() - elbows.get(i+1).getElement2();
			if(val > l2Diff) {
				l2Diff = val;
				elbowL2 = i;
			}	
		}
		
		//The score of the feature where the elbow occurs is the cutoff.
		int cutoff = elbows.get(elbowL2).getElement1();
		Set<Integer> toAdd = new HashSet<>();
		int pos = 0;
		while(pos < allFeatures.size() && allFeatures.get(pos).getElement1() != cutoff)
			toAdd.add(allFeatures.get(pos++).getElement1());
		toAdd.add(cutoff);
		
		//Rebuild the mapping
		for(int i = 0, added = 0; i < allFeatures.size(); ++i)
			if(toAdd.contains(i))
				mapping.put(i, added++);
			else
				mapping.put(i, SubspaceIdentification.UNUSED);
		return mapping;
	}

	@Override
	public void init(Properties props) throws Exception {
		//No parameters are associated with this class.
	}

}
