package mil.af.rl.predictive;

import java.util.Iterator;
import java.util.List;

/**
 * This interface enables multiple implementations of a observed sample from an interaction with the
 * environment to allow for time/space tradeoffs.
 * 
 * @author sloscal1
 *
 */
public interface Sample extends Cloneable{

	SampleType getType();
	
	/**
	 * @return the state
	 */
	Iterator<Double> getState();
	
	/**
	 * @return the action
	 */
	Iterator<Double> getAction();
	
	/**
	 * Builds a combined view of the state-action arrays.
	 * @return
	 */
	Iterator<Double> getStateAction();
	
	/**
	 * Gets the current state, action(s), and reward in that order.  It
	 * returns a new array object, changes made will not be reflected in 
	 * these elements.
	 * @return
	 */
	Iterator<Double> getStateActionReward();
	
	/**
	 * Builds a combined view of the state-action-next state arrays.
	 * @return
	 */
	Iterator<Double> getStateActionStatePrime();
	
	Iterator<Double> getSelectedStateActionReward(List<Integer> selected);
	
	Iterator<Double> getSelectedStateActionNextState(List<Integer> selected, int next);
	
	/**
	 * Get the total dimensionality of a sample as defined by the
	 * aggregate sizes of the state, action, next state, reward, and
	 * id components.
	 * @return
	 */
	int size();
	
	/**
	 * @return the statePrime
	 */
	Iterator<Double> getStatePrime();
	
	Iterator<Double> getStateActionSingleStatePrime(int i);
	
	double getState(int index);

	double getAction(int index);
	
	double getStatePrime(int index);
	
	/**
	 * @return the reward
	 */
	double getReward();
	
	double getBatchId();
	
	int getStateLength();
	
	int getActionLength();
	
	void setReward(double reward);
	
	Sample clone() throws CloneNotSupportedException;
	
	/**
	 * A label indicating the type of Sample that we're working with.  The agent
	 * tends to get this information while working in the environment, and it
	 * might be useful to store.
	 * 
	 * @author sloscal1
	 */
	public static enum SampleType{
		/** The agent has reached the goal state */
		GOAL,
		/** The agent has reached a failure state */
		FAILURE,
		/** The agent is in neither a goal nor failure state */
		NORMAL,
		/** The agent could be in any type of state */
		UNKNOWN
	}
}
