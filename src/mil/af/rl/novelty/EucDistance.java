package mil.af.rl.novelty;

import org.apache.commons.math3.ml.distance.EuclideanDistance;

public class EucDistance implements NoveltyDistanceFunction {
	private EuclideanDistance dist = new EuclideanDistance();
	
	@Override
	public double distance(double[] behavior1, double[] behavior2) {
		return dist.compute(behavior1, behavior2);
	}
}
