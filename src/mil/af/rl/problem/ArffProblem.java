package mil.af.rl.problem;

import java.util.Arrays;

import mil.af.rl.predictive.SubspaceIdentification;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import com.anji.util.Configurable;
import com.anji.util.Properties;

public class ArffProblem extends RLProblem implements Configurable{
	public final String DATA_FILE_KEY= "arff.problem.data_file";
	public final String NUM_SHUFFLES_KEY = "arff.problem.num_shuffles";
	public final String NUM_FOLDS_KEY = "arff.problem.num_folds";
	
	private int numShuffles;
	private int numFolds;
	private Instances dataFile;
	private Instances training;
	private double reward;
	private double fitness;
	private int instanceOn;
	private int foldOn;
	private int shuffleOn;
	private boolean finished;
	private int numTests = 0;
	
	@Override
	public void init(Properties props) throws Exception {
		super.init(props);
		this.numShuffles = props.getIntProperty(NUM_SHUFFLES_KEY);
		this.numFolds = props.getIntProperty(NUM_FOLDS_KEY);
		dataFile = new DataSource(props.getProperty(DATA_FILE_KEY)).getDataSet();
		dataFile.setClassIndex(dataFile.numAttributes()-1);
		state = new double[props.getIntProperty(SubspaceIdentification.FS_FEATURES_KEY)];
		training = dataFile.trainCV(numFolds, 0);
		Instance inst = training.get(0);
		for(int i = 0; i < state.length; ++i)
			state[i] = inst.value(i);
		initialState = state.clone();
		setstatelimits();
	}
	
	@Override
	protected void setstatelimits() {
		//Figure out the state limits
		statemax = new double[state.length];
		statemin = new double[state.length];
		Arrays.fill(statemax, -Double.MAX_VALUE);
		Arrays.fill(statemin, Double.MAX_VALUE);
		for(int i = 0; i < dataFile.numInstances(); ++i){
			for(int j = 0; j < state.length; ++j){
				double value = dataFile.instance(i).value(j);
				if(value > statemax[j])
					statemax[j] = value;
				if(value < statemin[j])
					statemin[j] = value;
			}
		}
	}

	@Override
	public void reset() {
		super.reset();
		instanceOn = 0;
		foldOn = 0;
		shuffleOn = 0;
		reward = 0;
		fitness = 0;
		finished = false;
//		System.out.println(numTests);
		numTests = 0;
	}
	@Override
	public void setRandomState(){
	}

	@Override
	public void setState(int stateindex) {

	}

	@Override
	public double doAction(int action) {
		throw new UnsupportedOperationException("Use the double version of this method.");
	}

	@Override
	public void doAction(double action) {
//		System.out.println("Num instances: "+training.numInstances());
//		System.out.println("Class value: ")
		reward = (action == training.instance(instanceOn).classValue())? 1.0 : 0.0;
		++numTests;
		fitness += reward;
		++instanceOn;
		if(instanceOn >= training.size()){
			instanceOn = 0;
			++foldOn;
			if(foldOn >= numFolds){
				foldOn = 0;
				++shuffleOn;
				if(shuffleOn >= numShuffles){
					shuffleOn = 0;
					finished = true;
				}
				else
					dataFile.randomize(random);
			}
			else
				training = dataFile.trainCV(numFolds, foldOn);
		}
		Instance inst = training.get(instanceOn);
		for(int i = 0; i < state.length; ++i)
			state[i] = inst.value(i); 
	}

	@Override
	public boolean inGoalState() {
		return false;
	}

	@Override
	public boolean inFailureState() {
		return finished;
	}

	@Override
	public double getReward() {
		return 0.0;
	}

	@Override
	public double getScore() {
		return fitness;
	}

	@Override
	public void applyActions(double[] actions) {
		double action = actions[0];
		if(action < 0.5)
			action = 0.0;
		else
			action = 1.0;
		doAction(action);
	}

}
