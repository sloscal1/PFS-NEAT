package mil.af.rl.predictive;

/**
 * Informational structure containing information pertaining to the stagnation
 * tracking methods.
 * 
 * @author sloscal1
 *
 */
public class StagnationInfo {
	/** Did perform feature selection since last stagnation check */
	public boolean didFeatureSelection;
	/** The current iteration the algorithm is on */
	public double iterationOn;
	/** The last iteration that feature selection was performed on */
	public double iterationOfSelection;
	/** The last seen fitness of the learner */
	public double lastPerformance;
}
