package mil.af.rl.predictive;

import java.util.Map;


import com.anji.util.Properties;

/**
 * This augments the typical RL algorithm with a few extra methods to allow it
 * to handle a changing dimensionality state space and the ability to collect
 * samples from the environment.
 * 
 * It seems like the setSampleContainer and setSampleCollection methods should be
 * part of a SampleGatherer interface which the ProgressiveLearner must have a
 * variable to refer to.  The problem is that the SampleGatherer may need some
 * deep access to the learner to be implemented efficiently.  I think that prohibits
 * it from getting teased out into a separate interface in some cases and so I
 * will leave it in this one for the time being.
 * 
 * @author sloscal1
 *
 */
public interface PredictiveLearner {
	static final String PROGRESSIVE_LEARNER_CLASS = "progressive_learner.class";
	/**
	 * Give a reference to the current sample container to store samples in.
	 * 
	 * @param container
	 */
	void setSampleContainer(SampleContainer container);
	
	/**
	 * Allow sample collection to be turned on or off in case there are storage
	 * or time considerations when gathering.
	 * @param collect
	 */
	void setSampleCollection(boolean collect);

	/**
	 * Uses this new mapping to resize the active state space.  The ProgressiveLearner
	 * is responsible for attempting to salvage any previously learned information
	 * or deciding to start again.
	 * 
	 * @param mapping Each key is a state dimension index (dependent on the problem
	 * environment) and each value is the dimension it should be mapped to in the
	 * reduced space.  A value of -1 indicates that is to be unused in resulting
	 * subspace.
	 */
	void setActiveSubspace(Map<Integer, Integer> mapping);
	
	/**
	 * This method returns the current active subspace mapping that is being
	 * used.
	 * 
	 * @return
	 */
	Map<Integer, Integer> getActiveSubspace();
	
	/**
	 * This method executes a single learning episode.  It is within this method
	 * that actual sample gathering will take place as well.
	 * 
	 * @return  
	 */
	double learn();
	
	/**
	 * 
	 * @return
	 */
	double getNumSamples();
	
	/**
	 * Provides any initialization parameters to be used by the ProgressiveLearner.
	 * 
	 * @param props a valid Properties object from a properties file.
	 * @throws Exception if some property does not exist, or the object cannot be
	 * initilialized properly
	 */
	void init(Properties props) throws Exception; 
	
	/**
	 * Checks to see that there is a previous state that the learner can revert
	 * to.  Alternatively, if this learner does not provide reversion capabilities,
	 * this method may always return false.
	 * @return true if the learner has a previous state that can be reverted to.
	 */
	boolean canRevert();

	/**
	 * Revert the learner to a previous state, typically defined by a different
	 * set of features from the one currently in use.  If there is no previous
	 * state to revert to when this call is made, no change happens in the learner.
	 * 
	 * @return the feature set that is reverted to, may be null.
	 */
	Map<Integer, Integer> revert();
	
	/**
	 * Inform the learner to set a reversion point at this instant, if reversion
	 * is supported by the learner.
	 */
	void setReversionPoint();
}
