package mil.af.rl.novelty;

import java.util.Random;

import org.jgap.Chromosome;

import com.anji.integration.Activator;
import com.anji.integration.ActivatorTranscriber;
import com.anji.integration.TranscriberException;
import com.anji.util.Configurable;
import com.anji.util.Properties;

import mil.af.rl.anji.learner.ConcurrentFitnessFunction;

public class RandomSampleBehaviorGenerator implements BehaviorVectorGenerator, Configurable {
	public static final String BG_NUM_SAMPLES = "behavior_vector_generator.num_samples";
	
	private Random rand;
	private ActivatorTranscriber factory;
	private double[][] inputs;
	private int responseSize;
	
	@Override
	public void init(Properties prop) throws Exception {
		rand = new Random(prop.getLongProperty("random.seed"));
		factory = (ActivatorTranscriber) prop.newObjectProperty(ConcurrentFitnessFunction.ACT_TRANS_KEY);
		//Go through and generate the state inputs to the chromosomes:
		inputs = new double[prop.getIntProperty(BG_NUM_SAMPLES)][prop.getIntProperty("stimulus.size")];
		for(int i = 0; i < inputs.length; ++i){
			for(int j = 0; j < inputs[i].length; ++j)
				inputs[i][j] = rand.nextDouble();
			//TODO check input node type to see if they need to be scaled to [-1,1]
		}
		responseSize = prop.getIntProperty("response.size");
	}
	
	@Override
	public double[] generateBehaviorVector(Chromosome chrom) {
		double[] retval = new double[inputs.length * responseSize];
		try {
			Activator activator = factory.newActivator(chrom);
			//Concatenate responses
			int base = 0;
			for(double[] input : inputs){
				double[] next = activator.next(input);
				for(int i = 0; i < next.length; ++i)
					retval[base+i] = next[i];
				base += next.length;
			}
		} catch (TranscriberException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retval;
	}
}
