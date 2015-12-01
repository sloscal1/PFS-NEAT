package mil.af.rl.novelty;

import java.util.Arrays;

import org.jgap.Chromosome;

import com.anji.util.Configurable;
import com.anji.util.Properties;

public class FitnessBasedBehaviorGenerator implements BehaviorVectorGenerator, Configurable {
	
	private int responseSize;
	
	@Override
	public void init(Properties prop) throws Exception {
		responseSize =prop.getIntProperty(RandomSampleBehaviorGenerator.BG_NUM_SAMPLES);
	}
	
	@Override
	public double[] generateBehaviorVector(Chromosome chrom) {
		double[] retval = new double[responseSize];
		
		Arrays.fill(retval, chrom.getFitnessValue());
		
		return retval;
	}
}
