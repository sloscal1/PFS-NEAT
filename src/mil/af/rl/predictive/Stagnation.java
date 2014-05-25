package mil.af.rl.predictive;

import com.anji.util.Configurable;


/**
 * Checks for learning performance stagnation.  Since stagnation can take on
 * many forms, it was decided that an interface would provide the most flexibility,
 * along with adopting a decorator pattern to allow for multiple simple stagnation
 * criteria to be chained together with little change to the framework.
 * 
 * Note that this (and other parts of the framework) might benefit from making
 * the learner Observable and making the Stagnation (and feature selection) an
 * Observer.
 * 
 * @author sloscal1
 *
 */
public interface Stagnation extends Configurable{
	static final String STAGNATION_BASE_CLASS = "stagnation.class";
	
	/**
	 * Keep the stagnation control device up-to-date on learning performance.
	 * 
	 * @param inidcators performance indication measures (may not be used)
	 */
	void updatePerformance(StagnationInfo info);
	
	/**
	 * True if this Stagnation object measures performance to be stagnant.
	 * @return
	 */
	boolean isStagnant();
	
	/**
	 * Sets the numbered properties that are associated with this object.
	 * @param count
	 */
	void setStagInitCount(int count);
}
