package mil.af.rl.predictive;

import weka.core.SelectedTag;

import com.anji.util.Configurable;
import com.anji.util.Properties;

/**
 * A simple adapter around weka.attributeSelection.BestFirst to make it configurable from
 * a properties file.  The start set must be specified programmatically at this time, but all
 * other options are properties file compliant.  Default values are used if the property does
 * not exist, these values are the same as used by the Weka implementation.
 * 
 * @author sloscal1
 *
 */
public class BestFirst extends weka.attributeSelection.BestFirst implements Configurable {
	/** Generated ID */
	private static final long serialVersionUID = -3844472669437160365L;
	/** Search direction (BACKWARD, FORWARD, BIDIRECTIONAL) case sensitive.  Defaults to FORWARD */
	public static final String BESTFIRST_DIRECTION = "weka.attributeSelection.BestFirst.direction";
	/** Number of non-improving nodes to consider before search termination. Defaults to 5 */
	public static final String BESTFIRST_TERM = "weka.attributeSelection.BestFirst.term";
	/** Size of lookup cache for evaluated subsets. Expressed as a multiple of number of attributes in a data set. Defaults to 1 */
	public static final String BESTFIRST_CACHE = "weka.attributeSelection.BestFirst.cache";

	@Override
	public void init(Properties props) throws Exception {
		setDirection(new SelectedTag(
				//Needs the index:
				SearchDirection.valueOf(props.getProperty(BESTFIRST_DIRECTION, //from props file 
						SearchDirection.FORWARD.toString())).ordinal(), //defaults to forward
				//And all possible tags
				TAGS_SELECTION));
		setSearchTermination(props.getIntProperty(BESTFIRST_TERM, 5));
		setLookupCacheSize(props.getIntProperty(BESTFIRST_CACHE, 1));
	}
	
	public static enum SearchDirection{
		BACKWARD,
		FORWARD,
		BIDIRECTIONAL,
	}
}
