package mil.af.rl.anji.learner;

import java.math.BigInteger;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import mil.af.rl.predictive.PFSInfo;
import mil.af.rl.problem.RLProblem;

import org.jgap.Chromosome;

import com.anji.integration.Activator;
import com.anji.integration.ActivatorTranscriber;
import com.anji.util.Properties;

/**
 * Abstract class for implementing learning methods over ANJI NN
 * @author Robert Wright
 * @author Steven Loscalzo (added PFSInfo to the interface of th evaluate method)
 *
 */
public abstract class RL_Learner {
	static Map<Long, Set<Double>> performances = new TreeMap<>();
	
	/**
	 * properties key, evaluate problem set
	 */
	public static final String USE_PROBLEM_SET_KEY = "use_problem_set";
	protected boolean useProblemSet = true;

	/**
	 * properties key, size of problem set
	 */
	public static final String SIZE_PROBLEM_SET_KEY = "problem_set_size";
	protected int problemSetSize = 20;

	/**
	 * properties key, evaluate random problem set
	 */
	public static final String USE_RANDOM_PROBLEM_SET_KEY = "random_problem_set";

	/**
	 * properties key, Learner Random Seed
	 */
	public static final String LEARNER_RANDOM_SEED_KEY = "random.seed";
	protected static Random rand;

	/**
	 * properties key, RL Problem
	 */
	public static final String PROBLEM_KEY = "problem";
	protected String problemClass = null;
	protected RLProblem problem = null;

	/**
	 * properties key, Problem Attempts
	 */
	public static final String PROBLEM_ATTEMPTS = "attempts";
	protected int attempts = 1;
	protected int MAX_FAILURES = 1;
	
	protected static int numActions = 2;
	
	protected Properties props;
	
	/**
	 * This is the number of updates that have occurred during the learning process 
	 */
	private static BigInteger numUpdates = BigInteger.ZERO;

	/**
	 * Initializes the object based on the properties file
	 */
	public void init(Properties props) throws Exception
	{
		this.props = props;
		useProblemSet = props.getBooleanProperty(USE_PROBLEM_SET_KEY,useProblemSet);
		problemSetSize = props.getIntProperty(SIZE_PROBLEM_SET_KEY,problemSetSize);
		if(props.getBooleanProperty(USE_RANDOM_PROBLEM_SET_KEY,!useProblemSet) && problemSetSize > 0)
			useProblemSet = false;

		problemClass = props.getProperty(PROBLEM_KEY+".class", null);
		if(problemClass!=null){
			problem = (RLProblem)props.singletonObjectProperty( PROBLEM_KEY );
		}else{
			System.out.println("NO PROBLEM SPECIFIED - Please include the name of a problem class in the properties file");
			System.exit(1);
		}
		rand = new Random(props.getIntProperty(LEARNER_RANDOM_SEED_KEY,5745));
		numActions = problem.numberOfActions();
	}
	
	public synchronized static void incUpdates(){
		numUpdates = numUpdates.add(BigInteger.ONE);
	}

	public synchronized static void incUpdates(int count){
		numUpdates = numUpdates.add(new BigInteger(""+count));
	}

	public synchronized static BigInteger getUpdates(){
		return numUpdates;
	}
	/**
	 * Evaluation function for a NN
	 * @param chrom The chromosomes that describe the NN
	 * @param factory The factory for building the NN from the chromosome
	 * @return The fitness of the NN
	 */
	public double evaluate(Chromosome chrom, ActivatorTranscriber factory, PFSInfo info)
	{
		//Evaluate fitness over the problem set specified
		double fitness = 0;		
		problem.resetRandom();
		
		Long key = chrom.getId();
		for (int i=0; i<this.problemSetSize; i++){
			if(useProblemSet){
				problem.setState(i);
			}else{
				problem.setRandomState();
			}
			
			double singleFit = run(chrom, factory, info);
			fitness += singleFit;
			synchronized(performances){
				if(!performances.containsKey(key))
					performances.put(key, new TreeSet<Double>());
				performances.get(key).add(singleFit);
			}
		}
		
		//Fitness is the average performance over the size of the problem set
		fitness/=(double)problemSetSize;
		
		return fitness;
	}
	
	/**
	 * This method performs a single evaluation
	 * @param chrom The chromosome for the NN
	 * @param factory The factory for building the NN from the chromosome
	 * @return The fitness
	 */
	protected abstract double run(Chromosome chrom, ActivatorTranscriber factory, PFSInfo info);		
	
	// a few more common functions
	
	protected double[] nextResponse(Activator activator)
	{
		double[] netinput = problem.getNetInput();
		return activator.next(netinput);
	}
	
	public static int getnumActions()
	{
		return numActions;
	}
}
