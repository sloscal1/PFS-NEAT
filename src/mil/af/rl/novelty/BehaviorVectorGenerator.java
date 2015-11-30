package mil.af.rl.novelty;

import org.jgap.Chromosome;

public interface BehaviorVectorGenerator {

	/**
	 * Generate this policy's behavior vector.
	 * 
	 * @param chrom non-null chromosome representing a policy function
	 * @return a non-null array representing this behavior vector
	 */
	double[] generateBehaviorVector(Chromosome chrom);
}
