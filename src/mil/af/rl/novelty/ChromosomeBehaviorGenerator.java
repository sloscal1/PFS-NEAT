package mil.af.rl.novelty;

import org.jgap.BehaviorChromosome;
import org.jgap.Chromosome;

public class ChromosomeBehaviorGenerator implements BehaviorVectorGenerator {

	@Override
	public double[] generateBehaviorVector(Chromosome chrom) {
		return ((BehaviorChromosome)chrom).getBehaviorVector();
	}

}
