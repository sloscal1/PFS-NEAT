package mil.af.rl.predictive;

import java.util.Collection;

import com.anji.util.Properties;

import mil.af.rl.predictive.Sample;
import mil.af.rl.predictive.SampleContainer;
import mil.af.rl.predictive.SampleSelector;

/**
 * The IdentitySampleSelector acts as a simple pass-through to the
 * underlying SampleContainer.  No sample selection logic is
 * implemented in this Selector.
 * 
 * @author sloscal1
 *
 */
public class IdentitySampleSelector implements SampleSelector {
	private SampleContainer store;
	
	@Override
	public void setSampleContainer(SampleContainer container) {
		this.store = container;
	}

	@Override
	public Collection<Sample> selectSamples() {
		return store.getSamples();
	}

	@Override
	public void init(Properties props) throws Exception {
		//NO ACTION
	}

	@Override
	public SampleContainer getSampleContainer() {
		return store;
	}
}
