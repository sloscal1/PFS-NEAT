package mil.af.rl.predictive;

import java.util.List;

import weka.core.Instance;

/**
 * This interface converts a raw sample object observed from the environment
 * into the type of data expected by the Model.
 * @author sloscal1
 *
 */
public interface DiscretizationProcess {
	enum ClassType{
		NOMINAL,
		NUMERIC
	}
	Instance convert(Sample sample);
	List<Integer> getSelectedFeatures();
	void setSelectedFeatures(List<Integer> features);
	ClassType getConvertedClassType();
}
