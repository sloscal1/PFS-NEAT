package mil.af.rl.predictive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import mil.af.rl.predictive.SampleUtilities.SampleRelationship;
import mil.af.rl.util.Incrementer;
import mil.af.rl.util.InstanceUtils;
import mil.af.rl.util.ListUtils;
import mil.af.rl.util.SimMeasures;

import org.apache.commons.lang3.ArrayUtils;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeEvaluator;
import weka.attributeSelection.StartSetHandler;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.Discretize;
import weka.filters.unsupervised.attribute.Center;

import com.anji.util.Configurable;
import com.anji.util.Properties;

public class WekaIncSubspaceIdentifier implements SubspaceIdentification {
	public static final String REWARD_SEARCH_METHOD = "weka.reward.search.class";
	public static final String REWARD_EVAL_METHOD = "weka.reward.eval.class";
	public static final String TRANS_SEARCH_METHOD = "weka.trans.search.class";
	public static final String TRANS_EVAL_METHOD = "weka.trans.eval.class";
	public static final String ADD_SINGLE_FEATURE = "weka.single.addition";

	/** I wonder what the selector is worth.  It really just seems to be a way to access the sample container */
	private SampleSelector selector;
	/** The number of state variables in the data set */
	private int numFeatures;
	/** The number of action variables in the data set */
	private int numActions;
	/** The set of reward relevant features */
	private List<Integer> rewardRelevant = new ArrayList<>();
	/** The set of transition relevant features */
	private Map<Integer, List<Integer>> transRelevant = new HashMap<>();
	/** Union of all features currently used in reward and transition relevant sets */
	private Set<Integer> policyRelevant = new HashSet<>();
	/** The indices of action variables */
	private List<Integer> actionIndices = new ArrayList<>();
	/** The priorities of transition relevant features */
	private Map<Integer, Priority> featurePriorities = new HashMap<>();
	private boolean oneAtATime;
	private Properties props;

	@Override
	public void init(Properties props) throws Exception {
		this.props = props;
		numFeatures = props.getIntProperty(SubspaceIdentification.FS_FEATURES_KEY);
		numActions = props.getIntProperty(SubspaceIdentification.FS_ACTIONS_KEY);
		for(int i = 0; i < numActions; ++i)
			actionIndices.add(numFeatures+i);
		rewardRelevant.addAll(actionIndices);
		oneAtATime = props.getBooleanProperty(ADD_SINGLE_FEATURE, false);
	}

	public Map<Integer, Integer> getSubspaceTransform() {
		//The return value
		Map<Integer, Integer> featureMap = null;
		//Transform the samples into Instances for the feature selection process
		Map<Integer, Integer> classLabels = new HashMap<Integer, Integer>();
		Instances fullInstances = processSamples(classLabels, false);
		List<Integer> featuresAndActions = new ArrayList<Integer>();

		//If a new feature could be found, look for it
		if(policyRelevant.size() != numFeatures){
			//First try and find a reward relevant feature
			List<Integer> next = selectFeatures(fullInstances, REWARD_SEARCH_METHOD, 
					REWARD_EVAL_METHOD, rewardRelevant);
			if(next.get(0) == SubspaceIdentification.UNUSED && rewardRelevant.size() > numActions){
				//This part evaluates all the transition features
				//Figure out what transition models to select (don't attempt to model action transitions)
				Set<Integer> featuresNoActions = new HashSet<Integer>();
				for(Integer i : featuresAndActions)
					if(i < numFeatures)
						featuresNoActions.add(i);
				System.out.println("Looking for transition relevant features");
				//Don't want to discretize the data
				fullInstances = processSamples(classLabels, false);
				System.out.println("About to stratify:");
				//Pull apart actions if we can, if continuous, just use one big chunk.
				List<Partition> actInstances = stratifyInstances(fullInstances);
				ExecutorService exec = Executors.newCachedThreadPool();
				List<Future<Void>> res = new ArrayList<>(actInstances.size() * rewardRelevant.size());

				//Clear out the old interested lists:
				for(Integer i : featurePriorities.keySet())
					featurePriorities.get(i).interested = new HashSet<Integer>();

				//Determine candidate feature priorities
				for(Partition part : actInstances)
					for(Integer featureOn : rewardRelevant)
						res.add(exec.submit(new FSTask(part, featureOn)));

				//Wait to make sure we have all priorities:
				ListUtils.waitForAll(res);

				//Have the priorities, determine which feature to select:
				//TODO: For now, only the max voted in feature is selected out of transition relevant features
				if(featurePriorities.size() != 0){
					next.clear();
					Integer max = Collections.max(featurePriorities.entrySet(), new Comparator<Entry<Integer, Priority>>(){
						@Override
						public int compare(Entry<Integer, Priority> o1,
								Entry<Integer, Priority> o2) {
							return o1.getValue().inc.getValue() - o2.getValue().inc.getValue();
						}
					}).getKey();

					next.add(max);

					for(Integer feature : featurePriorities.get(max).interested)
						transRelevant.get(feature).add(max);
					//Make it so that this feature isn't going to be considered again
					featurePriorities.remove(max);
				}
			} //Concludes the transition relevant feature selection section
			else if(next.get(0) != SubspaceIdentification.UNUSED){
				//Found a reward relevant feature
				rewardRelevant.addAll(next);
			}

			//Update the policy relevant feature subset (either reward or transition selected features go here)
			if(next.get(0) != SubspaceIdentification.UNUSED)
				policyRelevant.addAll(next);

			//Finally, build the mapping.
			featureMap = new LinkedHashMap<Integer, Integer>();
			int ind = 0;
			for(int i = 0; i < numFeatures; ++i){
				if(policyRelevant.contains(i))
					featureMap.put(i, ind++);
				else
					featureMap.put(i, SubspaceIdentification.UNUSED);
			}
			System.out.println("FEATURES: "+featureMap);
		}
		else{
			//All features have been selected, keep it that way.
			featureMap = new LinkedHashMap<Integer, Integer>();
			for(int i = 0; i < numFeatures; ++i)
				featureMap.put(i, i);
		}
		return featureMap;
	}

	private class FSTask implements Callable<Void>{
		private Partition part;
		private int featureOn;

		private FSTask(Partition part, int featureOn){
			this.part = part;
			this.featureOn = featureOn;
		}

		@Override
		public Void call() throws Exception {
			//Set the data for the particular partition we're testing
			Instances data = changeClassLabel(part, featureOn);
			System.out.println("Copied the data "+Thread.currentThread().getId());
			if(!transRelevant.containsKey(featureOn)){
				synchronized(transRelevant){
					if(!transRelevant.containsKey(featureOn)){
						transRelevant.put(featureOn, new ArrayList<Integer>());
						//Include the action features by default:
						transRelevant.get(featureOn).addAll(actionIndices);
					}
				}
			}
			//Find the next best feature
			List<Integer> candidates = selectFeatures(data, TRANS_SEARCH_METHOD,
					TRANS_EVAL_METHOD, transRelevant.get(featureOn));
			if(candidates.get(0) != SubspaceIdentification.UNUSED){
				for(Integer f : candidates){
					if(!policyRelevant.contains(f)){			
						//Update the priority of the selected feature
						synchronized(featurePriorities){
							if(!featurePriorities.containsKey(f))
								featurePriorities.put(f, new Priority());
							Priority p = featurePriorities.get(f);
							p.postInc();
							//Keep track of which features' transition models should be updated
							//if this feature is ultimately selected
							p.add(featureOn);					
						}
					}
				}
			}
			return null;
		}
	}

	private class Priority{
		private Incrementer inc;
		private Set<Integer> interested;

		private Priority(){
			this.inc = new Incrementer(0);
			this.interested = new HashSet<Integer>();
		}

		private int postInc(){
			return inc.postInc();
		}

		private boolean add(int featureOn){
			return interested.add(featureOn);
		}
	}

	/**
	 * Create a new Instances object that uses the next state value of featureOn
	 * from part.samples as the class label, and the rest of the Instances info
	 * from part.dataset. Part and its components must not be null.
	 *  
	 * @param part A pairing of Instances and corresponding Collection&ltSample&gt data.
	 * @param featureOn the feature thats next state information will be used as the class label.
	 * @return a new Instances object derived from part.dataset.
	 */
	private Instances changeClassLabel(Partition part, Integer featureOn) {
		Instances transInstances = InstanceUtils.changeClassLabelType(part.dataset, new Attribute("class"));
		//Change the data set so that the predictor is next state:
		int sOn = 0;
		Collection<Sample> fullData = part.samples;
		for(Sample s : fullData){
			transInstances.instance(sOn).setClassValue(s.getStatePrime(featureOn));
			transInstances.instance(sOn++).setDataset(transInstances);
		}
		return transInstances;
	}

	/**
	 * Update the selected feature subset based on this data set.  If the feature set
	 * changes from the previously selected subset (reward.selectedSubset), it will 
	 * also build a classifier using the specified modelClass
	 * and relMeasure.  The error of this model will also be measured according to
	 * the specified loss function.
	 * 
	 * @param data must be preprocessed to assure compatibility with the given
	 * model class. Must have a class variable.
	 * @param relMeasure must not be null
	 * @param modelClass must be a fully-qualified Weka classifier class name.
	 * @param l must not be null
	 * @param mustInclude the selected subset returned by this process must included these features (they must be
	 * valid feature indices for the data set).  Must not be null.
	 * @return null if no new features are selected by the process, otherwise,
	 * an FSInfo object containing the new model and selected subset.
	 */
	private List<Integer> selectFeatures(Instances data, String searchKey, 
			String evalKey, List<Integer> mustInclude){
		List<Integer> selectedFeatures = null;
		//Retrieve and initialize the search and evaluation procedures to be used
		ASSearch search = null;
		ASEvaluation eval = null;
		try {
			//Put the search object together
			search = (ASSearch)Class.forName(props.getProperty(searchKey)).newInstance();
			if(search instanceof Configurable)
				((Configurable)search).init(props);
			if(search instanceof StartSetHandler && mustInclude.size() > 0){
				StringBuilder sb = new StringBuilder();
				for(Integer i : mustInclude)
					sb.append((i+1)+",");
				sb.delete(sb.length()-1, sb.length());
				((StartSetHandler)search).setStartSet(sb.toString());
			}
			//Put the evaluation object together
			eval = (ASEvaluation)Class.forName(props.getProperty(evalKey)).newInstance();
			if(eval instanceof Configurable)
				((Configurable)eval).init(props);
		} catch (Exception e1) {
			e1.printStackTrace();
			System.exit(1);
		}
		//Actually do the work of evaluating and searching for the next subset to return
		try{
			eval.buildEvaluator(data);
			selectedFeatures = Arrays.asList(ArrayUtils.toObject(search.search(eval, data)));
			System.out.println("BESTFIRST: "+selectedFeatures);
		}catch(Exception e){
			e.printStackTrace();
			System.exit(1);
		}
		//Return the first feature that isn't already contained in must include.
		List<Integer> retFeatures = new ArrayList<>(1);
		if(selectedFeatures != null){
			for(Integer i : selectedFeatures){
				if(!mustInclude.contains(i) && i < numFeatures){
					retFeatures.add(i);
					//Ugh.  Should rewrite the loop.
					if(oneAtATime)
						break;
				}
			}
		}
		//If we get past the selected subset and nothing has changed, as there was no selection.
		if(retFeatures.size() == 0)
			retFeatures.add(SubspaceIdentification.UNUSED);
		return retFeatures;
	}

	public void revertToSubspace(Map<Integer, Integer> subspace){
		//Clear out the old information
		rewardRelevant = new ArrayList<>();
		for(Integer i : transRelevant.keySet()){
			transRelevant.put(i, new ArrayList<Integer>());
		}
		policyRelevant.clear();

		//Set the current set of features to what it should be.
		for(Integer i : subspace.keySet())
			if(subspace.get(i).intValue() != SubspaceIdentification.UNUSED)
				rewardRelevant.add(i);
		policyRelevant.addAll(rewardRelevant);
	}

	/**
	 * This method will partition full into n Instances objects, each one 
	 * collecting all Instance objects from full for a given combination of action
	 * variable values.  n is found by taking the Cartesian product of the number
	 * of values that each action variable can take with one another.  The
	 * action variables must take on no more than 10 distinct values or else they
	 * will be omitted from the n.
	 * @param full
	 * @return At most n Instances objects; every Instance object in full will be allocated
	 * to exactly one of the returned Instances objects.  May be empty if there
	 * are no discrete action variables in the problem.
	 */
	private List<Partition> stratifyInstances(Instances full){
		//Create the return structure
		List<Partition> ret = new ArrayList<Partition>();

		//Create an indexing tree to keep track of all possible inputs:
		Inner tree = null;

		//For each action attribute
		for(int i = 0; i < numActions; ++i){
			//Count the number of unique values for the action:
			int maxNums = 11;
			Set<Double> values = new HashSet<Double>(11);
			for(int j = 0; values.size() < maxNums && j < full.numInstances(); ++j){
				values.add(full.instance(j).value(numFeatures+i));
			}
			if(values.size() < maxNums){
				Double[] allValues = new Double[values.size()];
				int pos = 0;
				for(Double value : values)
					allValues[pos++] = value;
				Arrays.sort(allValues);
				if(tree == null)
					tree = new Inner(i, 0.0);
				tree.expand(i, allValues);
			}


		}
		//Finish off the tree
		if(tree != null){
			tree.createLeaves();

			//Now put the instances into the tree:
			Collection<Sample> samples = selector.getSampleContainer().getSamples();
			Iterator<Sample> sample = samples.iterator();
			for(int i = 0; i < full.numInstances(); ++i)
				tree.insertInstance(full.instance(i), full, sample.next());

			//Go through the trees to build up the returned set of Instances objects
			tree.gatherDatasets(ret);
		}
		//It could be the case that all action variables are continuous...
		else
			ret.add(new Partition(full, selector.getSampleContainer().getSamples()));

		return ret;
	}

	@Override
	public void updateLearningProgress(ProgressInfo info) {
		//Unused for the moment.
	}

	@Override
	public void setSampleSelector(SampleSelector selector) {
		this.selector = selector;
	}

	public static class PearsonAttributeEval extends ASEvaluation implements AttributeEvaluator{
		/**  Generated serial version id */
		private static final long serialVersionUID = -7435810126757784204L;

		private double[][] features;
		private double[] classVar;

		@Override
		public double evaluateAttribute(int attribute) throws Exception {
			return SimMeasures.pearson(features[attribute], classVar);
		}

		@Override
		public void buildEvaluator(Instances data) throws Exception {
			features = new double[data.numAttributes()-1][];
			for(int i = 0; i < data.numAttributes(); ++i)
				if(data.classIndex() != i)
					features[i] = data.attributeToDoubleArray(i);
				else
					classVar = data.attributeToDoubleArray(i);
		}
	}

	public static class RewardDiscretizer implements DiscretizationProcess{
		private Map<Integer, Integer> classLabels;
		private List<Integer> mapping;
		private Instances referenceDataSet;

		public RewardDiscretizer(Instances instances, Map<Integer, Integer> classLabels){
			//Need to copy the information, but not the data from instances:
			FastVector attrs = new FastVector();
			for(int i = 0; i < instances.numAttributes(); ++i)
				attrs.addElement(instances.attribute(i).copy());
			referenceDataSet = new Instances(instances.relationName(), attrs,0);
			referenceDataSet.setClassIndex(instances.classIndex());
			this.classLabels = classLabels;
		}

		@Override
		public Instance convert(Sample sample) {
			List<Double> data = SampleRelationship.STATE_ACTION_REWARD.convertToList(sample);
			//Trim down the data:
			List<Double> reduced = data;
			if(mapping != null){
				List<Double> selected = new ArrayList<Double>(data.size());
				for(int i : mapping)
					selected.add(data.get(i));
				reduced = new ArrayList<Double>(selected.size()+1);
				for(Double d : selected)
					reduced.add(d);
				reduced.add(data.get(data.size()-1)); //the class value
			}
			SampleUtilities.roundTo6Decimals(reduced);
			Integer label = classLabels.get(reduced.get(reduced.size()-1).intValue());
			if(label == null){ //Unseen class label:
				label = classLabels.size();
				classLabels.put(reduced.get(reduced.size()-1).intValue(), label);
			}
			reduced.set(reduced.size()-1, label.doubleValue());
			
			Instance instance = InstanceUtils.instanceFromList(1.0, reduced);
			instance.setDataset(referenceDataSet);
			//			referenceDataSet.add(instance);
			return instance;
		}

		@Override
		public void setSelectedFeatures(List<Integer> features) {
			this.mapping = features;			
		}

		@Override
		public ClassType getConvertedClassType() {
			return ClassType.NOMINAL;
		}

		@Override
		public List<Integer> getSelectedFeatures() {
			return mapping;
		}
	}

	public static class TransitionDiscretizer implements DiscretizationProcess{
		private int feature;
		private List<Integer> mapping;
		private Instances referenceDataSet;

		public TransitionDiscretizer(int feature, Instances dataSet){
			this.feature = feature;
			this.referenceDataSet = dataSet;
		}
		@Override
		public Instance convert(Sample sample) {
			Iterator<Double> sas = sample.getStateActionSingleStatePrime(feature);
			List<Double> data = new ArrayList<Double>();
			while(sas.hasNext())
				data.add(sas.next());
			//Trim down the data:
			List<Double> reduced = data;
			if(mapping != null){
				List<Double> selected = new ArrayList<Double>(data.size());
				for(int i : mapping)
					selected.add(data.get(i));
				reduced = new ArrayList<Double>(selected.size()+1);
				reduced.addAll(selected);
				reduced.add(data.get(data.size()-1)); //the class value
			}
			SampleUtilities.roundTo6Decimals(reduced);
			Instance retVal = InstanceUtils.instanceFromList(1.0, reduced);
			retVal.setDataset(referenceDataSet);
			return retVal;
		}

		@Override
		public void setSelectedFeatures(List<Integer> features) {
			this.mapping = features;			
		}

		@Override
		public ClassType getConvertedClassType() {
			return ClassType.NUMERIC;
		}
		@Override
		public List<Integer> getSelectedFeatures() {
			return mapping;
		}
	}

	//	private class Increment{
	//		double value;
	//
	//		private void inc(){
	//			++value;
	//		}
	//
	//		private void dec(){
	//			--value;
	//		}
	//	}
	//
	//	/**
	//	 * This causes the current models to be retrained using the same feature subsets
	//	 * but on the current set of samples.  This set of samples is expected to differ
	//	 * from the set that the models were originally trained on during the subspace
	//	 * identification.
	//	 */
	//	public void retrainModels() {
	//		//Get the data ready (might be a wasted step)
	//		Map<Integer, Integer> classLabels = new HashMap<Integer, Integer>();
	//		ModelAwareSampleContainer mCont = null;
	//		if(selector.getSampleContainer() instanceof ModelAwareSampleContainer)
	//			mCont = (ModelAwareSampleContainer)selector.getSampleContainer();
	//
	//		Collection<Sample> fullData = mCont.getSamples();
	//		Instances fullInstances = processSamples(fullData, classLabels);
	//
	//		if(mCont != null && mCont.getRewardModel() != null){
	//			//retrain the reward model
	//			Model rewardModel = mCont.getRewardModel();
	//			Instances reducedData = InstanceUtils.createInstances(fullInstances, rewardModel.getDiscretizationProcess().getSelectedFeatures());
	//			try{
	//				Classifier[] newFuncs = trainModels(reducedData, "weka.classifiers.trees.J48", 10);
	//				if(newFuncs[0] != null){
	//					rewardModel.updateFunction(newFuncs);
	//					rewardModel.setDiscretizationProcess(new RewardDiscretizer(reducedData, classLabels));
	//				}
	//			}catch(Exception e){
	//				e.printStackTrace();
	//				System.exit(0);
	//			}
	//		}
	//		for(int i = 0; i < transitions.length; ++i){
	//			FSInfo tm = transitions[i];
	//			if(tm != null){
	//				Instances reducedData = InstanceUtils.createInstances(fullInstances, tm.selectedSubset);
	//				try{
	//					Classifier[] newFuncs = trainModels(reducedData, "weka.classifiers.meta.AdditiveRegression", 10);
	//					if(newFuncs[0] != null)
	//						tm.updateFunction(newFuncs);
	//					if(mCont != null)
	//						mCont.setSingleTransitionModel(i, tm.model);
	//				}catch(Exception e){
	//					e.printStackTrace();
	//					System.exit(0);
	//				}
	//			}
	//		}
	//	}

	//	public Classifier[] trainModels(Instances data, String modelClass, int numFolds) throws Exception{
	//		Classifier[] func = new Classifier[numFolds];
	//		if(data.numInstances() > numFolds){
	//			for(int fold = 0; fold < numFolds; ++fold){
	//				Instances training = data.trainCV(numFolds, fold);
	//				//Doing a wrapper sort of implementation
	//				Classifier dt = (Classifier)Class.forName(modelClass).newInstance();
	//				dt.buildClassifier(training);
	//				func[fold] = dt;
	//			}
	//		}
	//		return func;
	//	}

	public Instances processSamples(Map<Integer, Integer> classLabels, boolean disc){
		Collection<Sample> fullData = selector.getSampleContainer().getSamples();	
		double[][] fullDataM = SampleUtilities.convertSamplesToMatrix(SampleRelationship.STATE_ACTION_REWARD, fullData);
		SampleUtilities.roundTo6Decimals(fullDataM);//Stupid thing for weka.
		//Want to discretize the reward label on type: TODO (for now)
		//First make a model of the reward function
		int dataLength = fullDataM[0].length;
		for(int sampOn = 0; sampOn < fullDataM.length; ++sampOn){
			fullDataM[sampOn][dataLength-1] = (int)fullDataM[sampOn][dataLength-1];
		}
		//Now, make the reward class label useful for weka
		int vPos = 0;
		for(double[] instance : fullDataM){
			int key = (int)instance[instance.length-1];
			if(!classLabels.containsKey(key)) //Weka doesn't like negative class labels
				classLabels.put(key, vPos++);
		}
		String[] values = new String[classLabels.size()];
		for(int i = 0; i < values.length; ++i)
			values[i] = ""+i;
		int classPos = fullDataM[0].length-1;

		//Convert all the data to the postive discretized values
		for(int i = 0; i < fullDataM.length; ++i){
			fullDataM[i][classPos] = classLabels.get((int)fullDataM[i][classPos]);
		}
		//				List<String> categories = new ArrayList<String>(); 
		//				for(SampleType type : SampleType.values()){
		//					categories.add(""+type.ordinal());
		//				}
		//				values = categories.toArray(new String[]{});
		Instances insts = InstanceUtils.createInstances(fullDataM, true, values);
		Filter center = new Center();
		Filter discretize = new Discretize();
		try {
			center.setInputFormat(insts);
			//			insts = Center.useFilter(insts, center);
			if(values.length > 1 && disc){
				discretize.setInputFormat(insts);
				//				insts = Discretize.useFilter(insts, discretize);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return insts;
	}

	private abstract class Node implements Comparable<Node>{
		private Double value;

		public Node(Double value){
			this.value = value;
		}

		public void expand(int position, Double[] values){
			//Do nothing on leaf nodes	
		}

		abstract void insertInstance(Instance inst, Instances src, Sample sample);

		public int compareTo(Node o) {
			double diff = value - o.value;
			if(diff < -1e-6)
				return -1;
			else if(diff > 1e-6)
				return 1;
			else
				return 0;
		}

		public abstract void gatherDatasets(List<Partition> datasets);

		public String toString() {
			return value.toString();
		}
	}

	private class Leaf extends Node{
		private Instances dataset;
		private List<Sample> samples;

		public Leaf(Double value) {
			super(value);
		}

		@Override
		void insertInstance(Instance inst, Instances src, Sample sample) {
			if(dataset == null){
				dataset = new Instances(src, 0);
				samples = new ArrayList<Sample>();
			}
			dataset.add(inst);
			inst.setDataset(dataset);
			samples.add(sample);
		}

		@Override
		public void gatherDatasets(List<Partition> datasets) {
			if(dataset != null){
				datasets.add(new Partition(dataset, samples));
			}
		}

	}

	private class Inner extends Node{
		private Node[] children;
		private int position;

		public Inner(int position, Double value){
			super(value);
			this.position = position;
		}

		public void expand(int position, Double[] values) {
			if(children == null){
				children = new Node[values.length];
				//The position of an inner node describes where your children are positioned:
				this.position = position;
				for(int i = 0; i < values.length; ++i)
					children[i] = new Inner(position, values[i]);
			}
			else
				for(Node n : children)
					n.expand(position, values);
		}

		@Override
		void insertInstance(Instance inst, Instances src, Sample sample) {
			//Figure out which node to send it to (must be in the array):
			int pos = Arrays.binarySearch(children, new Inner(position, inst.value(numFeatures + position)));
			children[pos].insertInstance(inst, src, sample);
		}

		public void createLeaves(){
			if(children != null && children[0] instanceof Inner){
				if(((Inner)children[0]).children == null){
					for(int i = 0; i < children.length; ++i)
						children[i] = new Leaf(children[i].value);
				}
				else
					for(Node n : children)
						((Inner)n).createLeaves();
			}
		}

		public void gatherDatasets(List<Partition> datasets) {
			for(Node n : children)
				n.gatherDatasets(datasets);
		}
	}

	private class Partition{
		Instances dataset;
		Collection<Sample> samples;

		private Partition(Instances dataset, Collection<Sample> samples){
			this.dataset = dataset;
			this.samples = samples;
		}
	}
}
