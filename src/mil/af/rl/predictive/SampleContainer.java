package mil.af.rl.predictive;

import java.io.IOException;
import java.util.Collection;

import mil.af.rl.predictive.Sample;


public interface SampleContainer {
	public static final String CONTAINER_CLASS = "container.class";
	public static final String MAX_SAMPLES = "container.max_samples";
	
	void clearSamples();
	void cullSamples();
	void addSample(Sample sample);
	void addSamples(Collection<Sample> samples);
	void addSamples(Sample[] samples);
	Collection<Sample> getSamples();
	void printSamples(String fileName) throws IOException;
}
