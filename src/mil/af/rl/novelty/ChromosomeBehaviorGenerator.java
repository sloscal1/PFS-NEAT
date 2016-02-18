package mil.af.rl.novelty;

import org.jgap.BehaviorChromosome;
import org.jgap.Chromosome;

/**
 * This class should be used as the <code>novelty_obj.behavior_generator.class</code>
 * property whenever the Chromosomes used by NEAT store their own behavior vectors.
 * This is often useful when used in conjunction with RemoteLearner and the
 * remote problem class computes and sends the behavior vector back to NEAT.
 * <p>
 * Note: This way of working with behavior vectors will only work when using
 * NoveltyGenotypeAdapter in the Evolver class, which is also a necessity for
 * using any novelty search functionality in this code.
 * 
 * @author sloscal1
 *
 */
public class ChromosomeBehaviorGenerator implements BehaviorVectorGenerator {

	@Override
	public double[] generateBehaviorVector(Chromosome chrom) {
		return ((BehaviorChromosome)chrom).getBehaviorVector();
	}

}
