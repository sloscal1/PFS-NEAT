package mil.af.rl.predictive;

import java.util.Collection;

import mil.af.rl.predictive.Sample;

import com.anji.util.Properties;


/**
 * SampleSelector.
 * 
 * @author sloscal1
 *
 */
public interface SampleSelector {
	public static final String SELECTOR_CLASS = "selector.class";
	
	public abstract void setSampleContainer(SampleContainer container);
	
	public abstract Collection<Sample> selectSamples();
	
	public abstract void init(Properties props) throws Exception;

	public abstract SampleContainer getSampleContainer();
	
}
