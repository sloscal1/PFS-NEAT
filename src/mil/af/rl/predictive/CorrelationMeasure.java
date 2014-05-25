package mil.af.rl.predictive;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;

public enum CorrelationMeasure {

	PEARSON{
		private PearsonsCorrelation pc = new PearsonsCorrelation();
		@Override
		public double getMeasure(double[] v1, double[] v2) {
			return pc.correlation(v1, v2);
		}
	};
	
	public abstract double getMeasure(double[] v1, double[] v2);
}
