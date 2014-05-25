package mil.af.rl.problem;

import java.util.Observable;
import java.util.Random;

import javax.naming.SizeLimitExceededException;

import mil.af.rl.util.DoubleIndexPair;

import com.anji.util.Properties;

/**
 * This abstract class is a template for creating RL problems to integrate with the RL-SANE algorithm
 * 
 * @author Robert Wright
 * modified by Stephen Lin
 */
public abstract class RLProblem extends Observable
{
	/** properties key, Random Seed */
	public static final String RANDOM_SEED_KEY = "learner.problem.random";
	/** properties key, MAX Steps */
	public static final String MAX_STEPS_KEY = "learner.problem.maxsteps";
	
	public static final String STIMULUS_SIZE_KEY = "stimulus.size";
	public static final String RESPONSE_SIZE_KEY = "response.size";

//	public static final double[] NetMinMax = {0.0, 1.0};
	public static final double[] NetMinMax = {-1.0, 1.0};
	
	protected double [] state = null;
	protected double [] statemin = null, statemax = null;
	protected static int numActions = -1, numPerceptions = -1;
	protected double [] initialState = null; //The initial state of the problem
	
	protected long currentTimeStep = 0;
	
	protected int randomSeed = 5745;
	protected Random random = null;
	
	protected static int MAX_STEPS = -1;
	
	public void init(Properties props) throws Exception
	{
		try
		{
			randomSeed = props.getIntProperty(RANDOM_SEED_KEY, 5745);
			randomSeed = props.getIntProperty("random", randomSeed);
			
			MAX_STEPS = props.getIntProperty(MAX_STEPS_KEY, MAX_STEPS);
			MAX_STEPS = props.getIntProperty("maxsteps", MAX_STEPS);
						
		}
		catch (Exception e)
		{
			throw new IllegalArgumentException( "Invalid Properties: " + e.getClass().toString() + ": " + e.getMessage() );
		}
	}
	
	final protected static void setMaxSteps(int steps)
	{
		if(MAX_STEPS == -1)
			MAX_STEPS = steps;
	}
	
	public void resetMaxSteps()
	{
		MAX_STEPS = -1;
	}
	
	protected abstract void setstatelimits();
	
	public double boundstate(int stateindex)
	{
		state[stateindex] = boundedvalue(state[stateindex], statemin[stateindex], statemax[stateindex]);
		
		return state[stateindex];
	}
	
	public static double boundedvalue(double value, double min, double max)
	{
		if(value < min)
			value = min;
		else if(value > max)
			value = max;
		
		return value;
	}
	
	
	/**
	 * Resets the problem to the initial state and resets the current time step to 0
	 */
	public void reset()
	{		
		state = initialState.clone();
		currentTimeStep = 0;
	}
	
	/**
	 * Sets the state of the problem to a random start position based on the seed
	 * 
	 * NOTE: This will reset the problem
	 */
	public abstract void setRandomState();
	
	/**
	 * Set the state of the problem
	 * 
	 * NOTE: This will reset the problem
	 * @param state The new state of the problem
	 */
	public void setState(double[] statevalue)
	{
		initialState = statevalue.clone();
		reset();
	}
	
	/**
	 * Set the state of the problem
	 * 
	 * NOTE: This will reset the problem
	 * @param state The new state of the problem from the specified problem set
	 */
	public abstract void setState(int stateindex);
	
	/**
	 * Returns the current state of the problem
	 * @return The current state of the problem
	 */
	public double [] getState(){
		return state.clone();
	}
	
	/**
	 * Apply an action to the problem
	 * @param action The action to take
	 */
	public void applyAction(int action)
	{
		double actionC = doAction(action);
		
		if((actionC >= 0.0) && (actionC <= 1.0))
			applyAction(actionC);
		else
			timestep(action);
	}
	
	// input continuous action assumed to be [0, 1]
	public void applyAction(double action)
	{
		double act = (action * 2.0) - 1.0;
		
		doAction(act);
		timestep(action);
	}
	
	public abstract double doAction(int action);
	public abstract void doAction(double action);
	
	public void timestep(double action)
	{
		currentTimeStep++;  //must increment the time step
	}
		
	/**
	 * Returns true if the problem has been solved
	 * @return True if the problem has been solved
	 */
	public abstract boolean inGoalState();
	
	/**
	 * Returns true if the problem is in a failure state
	 * @return True if the problem is in a failure state
	 */
	public abstract boolean inFailureState();

	/**
	 * 
	 * @return The current time step of the problem
	 */
	public long getCurrentTimeStep() {
		return currentTimeStep;
	}
	
	/**
	 * Returns the reward for the current state
	 * @return the reward
	 */
	public abstract double getReward();
	
	/**
	 * Returns the number of actions available for this problem
	 * @return Number of Actions
	 */
	public int numberOfActions()
	{
		return numActions;
	}
	
	/**
	 * Returns the number of perceptions available
	 * @return Number of Perceptions
	 */
	public int numberOfPerceptions()
	{
		return numPerceptions;
	}

	public static double[] getNetMinMax()
	{
		return NetMinMax;
	}
	
	public void setNetInput(double[] stimuli)
	{
		double[] tempstate = stimuli.clone();
		int i = 0;
		
		for(i = 0; i < tempstate.length; i++)
			tempstate[i] = (stimuli[i] * ((statemax[i] - statemin[i]) / 2.0)) + ((statemax[i] + statemin[i]) / 2.0);
		
		setState(tempstate);
	}
	
	/**
	 * Returns an array of perception input for the neural network
	 * The perception values are scaled between [-1..1] or [0..1]
	 * @return Array of perception values
	 */
	public double[] getNetInput()
	{
		double[] stimuli = null;

		try
		{
			stimuli = produceNetInput();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return stimuli;
	}
	
	protected double[] produceNetInput() throws SizeLimitExceededException
	{
		if(state == null)
			return null;

		int i = 0;
		double[] netInput = new double[state.length];

		if(NetMinMax[0] == 0.0)
		{
			for(i = 0; i < netInput.length; i++)
			{
				// [0..1]
				netInput[i] = (state[i] - statemin[i]) / (statemax[i] - statemin[i]);
				if((netInput[i] < 0.0) || (netInput[i] > 1.0))
					throw new SizeLimitExceededException("NetInput[" + i + "] = " + netInput[i] + "\t");
			}
		}
		else
		{
			for(i = 0; i < netInput.length; i++)
			{
				// [-1..1]
				netInput[i] = 2.0 * (state[i] - ((statemax[i] + statemin[i]) / 2.0)) / (statemax[i] - statemin[i]);	
				if((netInput[i] < -1.0) || (netInput[i] > 1.0))
					throw new SizeLimitExceededException("NetInput[" + i + "] = " + netInput[i] + "\t");	
			}
		}

		return netInput;
	}	
	
	/**
	 * Returns the fitness metric of how the agent performed over the problem instance
	 * @return the fitness score
	 */
	public abstract double getScore();
	
	/**
	 * Resets the problems random number generator
	 */
	public void resetRandom(){
		random = new Random(randomSeed);
	}
	public void setRandomSeed(long seed)
	{
		random = new Random();
	}

	/**
	 * This method gets the upper and lower boundaries for a particular dimension i.
	 * 
	 * @param i the perception to get boundaries for.
	 * @return A pair object containing the boundaries in the form (lowerBound, upperBound).
	 * @throws IllegalArgumentException if the Dimension does not exist
	 */
	public DoubleIndexPair<Double, Double> getBoundariesForDimension(int i) throws IllegalArgumentException
	{
		if((i < 0) || (i >= numPerceptions))
			throw new IllegalArgumentException("Dimension does not exist: " + i);

		return new DoubleIndexPair<Double, Double>(statemin[i], statemax[i]);
	}

	/**
	 * @return
	 */
	public String[] getPerceptionNames(){
		String[] names = new String[numberOfPerceptions()];
		for(int i = 0; i < names.length; ++i)
			names[i] = "p"+(i+1);
		return names;
	}
	
	/**
	 * gaussian random variable centered at 0.0, bounded at specified magnitude.
	 * value = (java.util.Random.nextGaussian() / 5.0) * magnitude;
	 * captures widthfactor std dev from mean
	 * @param magnitude bounds of the return value
	 * @return gaussian distributed random value between [-magnitude, magnitude]
	 */
	public static double getBoundedGaussian(Random rand, double magnitude)
	{
		double value = rand.nextGaussian();
		
		value = (value / 5.0) * magnitude;
		
		if(Math.abs(value) > magnitude)
			value = Math.signum(value) * magnitude;
		
		return value;
	}
	
	public double[] getStateMin(){
		return statemin;
	}
	
	public double[] getStateMax(){
		return statemax;
	}


	public long getRandomSeed() {
		return randomSeed;
	}
	

	/**
	 * Return a terminal state (one that is reached whenever learning has completed,
	 * either with a success or failure).  By definintion, the only outgoing transition
	 * from this state is a self-loop with 0 reward.
	 * @return A state representation that corresponds to the terminal state.  It should
	 * not overlap with any other state in the state space.
	 */
	public double[] getTerminalState(){
		throw new UnsupportedOperationException("The problem does not yet define a terminal state.");
	}
	
	public abstract void applyActions(double[] actions);
}
