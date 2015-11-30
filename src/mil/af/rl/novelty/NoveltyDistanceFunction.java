package mil.af.rl.novelty;

public interface NoveltyDistanceFunction {

	/**
	 * Measure the behavior distance between two behavior characterization
	 * vectors. The relative novelty score is returned.
	 * 
	 * @param behavior1 must not be null
	 * @param behavior2 must not be null
	 * @return a non-negative real number
	 */
	double distance(double[] behavior1, double[] behavior2);
}
