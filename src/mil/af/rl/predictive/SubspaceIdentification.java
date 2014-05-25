package mil.af.rl.predictive;

import java.util.Map;

import com.anji.util.Properties;

/**
 * The general contract of the Feature Selection agent in the progressive framework.
 * The agent must be able to select features given the data collected in the
 * SampleContainer.  Optionally, the agent may receive additional learning progress
 * information from the RL agent, and that information will necessarily vary depending
 * on the RL agent used.
 * 
 * @author sloscal1
 *
 */
public interface SubspaceIdentification {
	/** The properties file key indicating the concrete implementation to use within the framework */
	public static final String SUBSPACE_CLASS = "subspace_identification.class";
	/** The minimum effective number of samples necessary by this algorithm */
	public static final String MIN_SAMPLES_KEY = "subspace.min_samples";
	/** Properties file key for number of action variables to measure relationships against */
	public static final String FS_ACTIONS_KEY = "subspace.actions";
	/** Properties file key for number of features to select from */
	public static final String FS_FEATURES_KEY = "subspace.features";
	/** Properties file key controlling the discretization of the data set */
	public static final String FS_DISCRETIZE_KEY = "subspace.discretize";
	/** Properties file key controlling the number of bins in a discretized feature */
	public static final String FS_DISCRETIZE_PREDICTORS_KEY = "subspace.discretize.predictors.bins";
	/** Properties file key controlling the number of bins in a discretized class variable */
	public static final String FS_DISCRETIZE_RESPONSE_KEY = "subspace.discretize.response.bins";
	/** A dimension mapping to UNUSED indicates that it is... unused */
	final int UNUSED = -1;
	
	/**
	 * Provides any initialization parameters to be used by the ProgressiveLearner.
	 * 
	 * @param props a valid Properties object from a properties file.
	 * @throws Exception if some property does not exist, or the object cannot be
	 * initilialized properly
	 */
	void init(Properties props) throws Exception;
	
	/**
	 * This causes feature selection to be done, gathering samples from the current
	 * selector and using the current ProgressInfo object.
	 * @return A map where keys are feature indices and values are the unique
	 * selected rankings.  A value of -1 indicates that the index is not selected.
	 */
	Map<Integer, Integer> getSubspaceTransform();
	
	/**
	 * Update any internal state necessary to reflect the given subspace is the
	 * one to base future execution on.
	 * 
	 * @param subspace must not be null
	 */
	void revertToSubspace(Map<Integer, Integer> subspace);
	
	/**
	 * Set the new progress that the agent has made (optional)
	 * @param agent
	 */
	void updateLearningProgress(ProgressInfo info);
	
	/**
	 * Set the sample selector to use for gathering samples.
	 * @param selector
	 */
	void setSampleSelector(SampleSelector selector);

}
