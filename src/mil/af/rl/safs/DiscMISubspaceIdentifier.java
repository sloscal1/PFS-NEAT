package mil.af.rl.safs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import mil.af.rl.predictive.ProgressInfo;
import mil.af.rl.predictive.Sample;
import mil.af.rl.predictive.SampleSelector;
import mil.af.rl.predictive.SampleUtilities;
import mil.af.rl.predictive.SubspaceIdentification;
import mil.af.rl.predictive.SampleUtilities.SampleRelationship;
import mil.af.rl.util.DoubleIndexPair;
import mil.af.rl.util.InstanceUtils;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.Standardize;

import com.anji.util.Properties;

public class DiscMISubspaceIdentifier implements SubspaceIdentification {
	public static final String SAMPLE_RELATIONSHIP_KEY = "subspace_identification.dmi.sample_relationship";
	public static final String FS_STRATEGY_KEY = "subspace_identification.dmi.strategy";

	private InfoGainAttributeEval[] evaluators;
	private Map<Integer, Integer> spaceTransformMap;
	private SampleSelector selector;
	private SelectionStrategy strat;
	private SampleRelationship rel;
	// variables controlling discretization
	private int numResponseBins = 10;
	private int numPredictorBins = 19;

	public DiscMISubspaceIdentifier(SampleRelationship rel) {
		this.rel = rel;
		strat = new SortedElbowCriteria();
	}

	public DiscMISubspaceIdentifier(){}

	@Override
	public Map<Integer, Integer> getSubspaceTransform() {
		// Get the Instances objects for the evaluation

		// This is S,A
		Collection<Sample> samples = selector.selectSamples();
		if (samples.size() > 0) {
			for (int a = 0; a < evaluators.length; ++a) {
				try {
					rel.setResonse(a);
					double[][] sampleData = SampleUtilities
							.convertSamplesToMatrix(rel, samples);

					String[] classLabels = null;

					SampleUtilities.equalFreqBinning(sampleData,
							numPredictorBins, numResponseBins,
							spaceTransformMap.size());
					// Get the class labels
					classLabels = new String[numResponseBins];
					for (int i = 0; i < numResponseBins; ++i)
						classLabels[i] = "" + i;

					// Make the weka data object
					Instances d = InstanceUtils.createInstances(sampleData,
							true, false, rel.toString() + a, null, classLabels);
					// Finish off the discretization if necessary
					Standardize stnd = new Standardize();
					d.setClassIndex(d.numAttributes() - 1);
					stnd.setInputFormat(d);
					d = Filter.useFilter(d, stnd);

					Discretize filter = new Discretize();
					filter.setInputFormat(d);
					d.setClassIndex(d.numAttributes() - 1);
					filter.setIgnoreClass(false);
					filter.setBins(numPredictorBins);
					filter.setUseEqualFrequency(false);
					d = Filter.useFilter(d, filter);
					d.setClassIndex(d.numAttributes() - 1);
					evaluators[a].buildEvaluator(d);
				} catch (Exception e) {
					e.printStackTrace();
					System.exit(1);
				}
			}

			// Do the feature evaluation:
			double[][] scores = new double[spaceTransformMap.size()][evaluators.length];
			// Keep track of the min and max value for scaling purposes
			List<DoubleIndexPair<Double, Double>> minMax = new ArrayList<DoubleIndexPair<Double, Double>>(
					evaluators.length);
			for (int i = 0; i < evaluators.length; ++i)
				minMax.add(new DoubleIndexPair<Double, Double>(Double.MAX_VALUE,
						-Double.MAX_VALUE));

			for (Integer feature : spaceTransformMap.keySet()) {
				if(spaceTransformMap.get(feature) == SubspaceIdentification.UNUSED)
					for (int i = 0; i < evaluators.length; ++i) {
						try {
							double score = evaluators[i].evaluateAttribute(feature);

							scores[feature][i] = score;
							// Do the min max calc
							if (scores[feature][i] > minMax.get(i).getElement2())
								minMax.get(i).setElement2(scores[feature][i]);
							if (scores[feature][i] < minMax.get(i).getElement1())
								minMax.get(i).setElement1(scores[feature][i]);
						} catch (Exception e) {
							//This could happen if the evaluateAttribute method has some trouble.
							e.printStackTrace();
						}

					}
				else
					for(int i = 0; i < evaluators.length; ++i)
						scores[feature][i] = -1.0; //Make this feature undesireable to select (again)
			}

			// Do the feature ranking and selection
			List<DoubleIndexPair<Integer, Double>> featureScores = new ArrayList<DoubleIndexPair<Integer, Double>>();

			for (int i = 0; i < scores.length; ++i) {
				double best = -Double.MAX_VALUE;
				for (int j = 0; j < scores[i].length; ++j) 
					best = Math.max(best, scores[i][j]);
				featureScores.add(new DoubleIndexPair<Integer, Double>(i, best));

			}

			// Simple top k type of thing:
			Collections.sort(featureScores);// It's ascending to go from the
			// back:
			Collections.reverse(featureScores);
			spaceTransformMap = strat.selectFeatures(featureScores);
		}

		return Collections.unmodifiableMap(spaceTransformMap);
	}

	@Override
	public void init(Properties props) throws Exception {
		int numActions = props.getIntProperty(FS_ACTIONS_KEY);
		int numStateVars = props.getIntProperty(FS_FEATURES_KEY);

		evaluators = new InfoGainAttributeEval[numActions];
		for (int i = 0; i < numActions; ++i) {
			evaluators[i] = new InfoGainAttributeEval();
			// Put in any options here
		}
		spaceTransformMap = new LinkedHashMap<Integer, Integer>(numStateVars);
		for (int i = 0; i < numStateVars; ++i)
			spaceTransformMap.put(i, SubspaceIdentification.UNUSED); // initially identity map

		// discritization is fixed to true
		numResponseBins = props.getIntProperty(FS_DISCRETIZE_RESPONSE_KEY);
		numPredictorBins = props.getIntProperty(FS_DISCRETIZE_PREDICTORS_KEY);
		rel = SampleRelationship.valueOf(props.getProperty(SAMPLE_RELATIONSHIP_KEY));
		strat = (SelectionStrategy)Class.forName(props.getProperty(FS_STRATEGY_KEY)).newInstance();
	}

	@Override
	public void setSampleSelector(SampleSelector selector) {
		this.selector = selector;

	}

	@Override
	public void updateLearningProgress(ProgressInfo info) {
		//Nothing to do here...
	}

	@Override
	public void revertToSubspace(Map<Integer, Integer> subspace) {
		//Only the current map needs to be changed, there is no other preserved
		//state
		spaceTransformMap = new LinkedHashMap<>(subspace);		
	}

}
