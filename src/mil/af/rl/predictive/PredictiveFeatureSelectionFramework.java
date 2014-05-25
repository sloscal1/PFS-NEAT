package mil.af.rl.predictive;

import static mil.af.rl.predictive.PredictiveLearner.PROGRESSIVE_LEARNER_CLASS;
import static mil.af.rl.predictive.SampleContainer.CONTAINER_CLASS;
import static mil.af.rl.predictive.SampleSelector.SELECTOR_CLASS;
import static mil.af.rl.predictive.Stagnation.STAGNATION_BASE_CLASS;
import static mil.af.rl.predictive.SubspaceIdentification.*;

import java.util.LinkedHashMap;
import java.util.Map;

import mil.af.rl.predictive.PredictiveLearner;
import mil.af.rl.predictive.SampleContainer;
import mil.af.rl.predictive.SampleSelector;
import mil.af.rl.predictive.Stagnation;
import mil.af.rl.predictive.StagnationInfo;
import mil.af.rl.predictive.SubspaceIdentification;

import com.anji.util.Configurable;
import com.anji.util.Properties;

/**
 * This class can be used to implement the SAFS-NEAT algorithm described in:
 * <br />
 * Loscalzo, S., Wright, R., Acunto, K., and Yu, Lei. Sample Aware Feature Selection
 * for Reinforcement Learning.  GECCO, 2012, pages 874 - 886,
 * <br />
 * or the PFS-NEAT algorithm described in:
 * Loscalzo, S., Wright, R., and Yu, Lei. Predictive Feature Selection for Genetic
 * Policy Search. To appear in JAAMAS 2014.
 * 
 * 
 * @author sloscal1
 *
 */
public class PredictiveFeatureSelectionFramework implements Configurable {
	/** The number of iterations the learner will iterate (same as number of evolutions if NEAT is the learner */
	public static final String LEARNER_ITERATIONS_KEY = "learner.iterations";
	/** Determine the minimum number of generations to observe before referting in the case
	 *  of a negative feature selection result. */
	public static final String REVERSION_WINDOW = "learner.reversion_window";

	private int maxIterations;
	private int minSamples;
	private int reversionWindow;
	private SampleContainer store;
	private SubspaceIdentification fs;
	private Stagnation stag;
	private PredictiveLearner agent;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		PredictiveFeatureSelectionFramework alg = new PredictiveFeatureSelectionFramework();
		//Initialize the framework to the specified parameters
		alg.init(new Properties(args[0]));
		//Perform the SAFS algorithm
		alg.doPFS();
	}

	public void doPFS(){
		//These variables facilitate the rollback functionality as well as
		//early feature selection stopping.
		boolean didInitialSelection = false;
		double stagPerformance = -1.0;
		StagnationInfo stagInfo = new StagnationInfo();

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//The main learning loop
		do{
			//Let the learner work, keep track of samples
			stagInfo.lastPerformance  = agent.learn();
			stag.updatePerformance(stagInfo);
			stagInfo.didFeatureSelection = false;
			//Keep the number of samples reasonable
			store.cullSamples();

			//Determine what change (if any) should happen to the selected subset
			//Should we revert to the previous subset?
			if(stagInfo.lastPerformance < stagPerformance //Is the current subset hurting performance
					&& stagInfo.iterationOn - stagInfo.iterationOfSelection >= reversionWindow
					&& agent.canRevert()){ //Did the learner have time to adapt to the current subset				
				//If we have already reverted, there may be no point in trying to do so again
				agent.revert();
				fs.revertToSubspace(agent.getActiveSubspace());
				System.out.println("REVERTING: "+agent.getActiveSubspace());

				stagInfo.didFeatureSelection = true;
				stagInfo.iterationOfSelection = stagInfo.iterationOn;	
			}
			//Should a new subset be selected?
			else if(!didInitialSelection //Need to initiate the process?
						|| stag.isStagnant()){ //Has learning hit a wall?
				//Want to keep track of the best performance thus far
				if(stagPerformance < stagInfo.lastPerformance && didInitialSelection){ //Don't revert back to the full starting set
					stagPerformance = stagInfo.lastPerformance;
					agent.setReversionPoint();
				}
				//Perform feature selection
				if(store.getSamples().size() >= minSamples){
					Map<Integer, Integer> selected = fs.getSubspaceTransform();

					System.out.println("SELECTED @ "+stagInfo.iterationOn+": "+selected);
					if(selected.values().contains(0)){ //Only replace the subset if it is non-empty!, first feature is 0th selected
						agent.setActiveSubspace(selected);
						//Bookkeeping
						stagInfo.didFeatureSelection = true;
						stagInfo.iterationOfSelection = stagInfo.iterationOn;
						didInitialSelection = true;
					}
				}
			}
			++stagInfo.iterationOn;
			System.out.println(stagInfo.lastPerformance);
			System.out.println(store.getSamples().size());
		}while(stagInfo.iterationOn < maxIterations && stagInfo.lastPerformance < 6.0);
	}

	@Override
	public void init(Properties props) throws Exception {
		//Set the simple framework parameters
		maxIterations = props.getIntProperty(LEARNER_ITERATIONS_KEY);
		minSamples = props.getIntProperty(MIN_SAMPLES_KEY);
		reversionWindow = props.getIntProperty(REVERSION_WINDOW);

		//Set up the sample store
		store = (SampleContainer)Class.forName(props.getProperty(CONTAINER_CLASS)).newInstance();
		if(store instanceof Configurable)
			((Configurable)store).init(props);

		//Set up the sample selector
		SampleSelector selector =(SampleSelector)Class.forName(props.getProperty(SELECTOR_CLASS)).newInstance();
		selector.init(props);
		selector.setSampleContainer(store);

		//Set up the feature selection algorithm
		fs = (SubspaceIdentification)Class.forName(props.getProperty(SUBSPACE_CLASS)).newInstance();
		fs.init(props);
		fs.setSampleSelector(selector);

		//Set up the stagnation criteria
		stag = (Stagnation)Class.forName(props.getProperty(STAGNATION_BASE_CLASS)).newInstance();
		stag.init(props);

		//Set up the learner
		int numFeatures = props
				.getIntProperty(FS_FEATURES_KEY);
		agent = (PredictiveLearner)Class.forName(props.getProperty(PROGRESSIVE_LEARNER_CLASS)).newInstance();
		agent.init(props);
		agent.setSampleContainer(store);
		agent.setSampleCollection(true);
		Map<Integer, Integer> full = new LinkedHashMap<Integer, Integer>(numFeatures);
		for(int i = 0; i < numFeatures; ++i)
			full.put(i, i);
		agent.setActiveSubspace(full);
	}
}
