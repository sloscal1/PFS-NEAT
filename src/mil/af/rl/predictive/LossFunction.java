package mil.af.rl.predictive;


/**
 * A collection of ssimple loss functions to measure error between values.
 * 
 * @author sloscal1
 *
 */
public enum LossFunction {
	ZERO_ONE{
		@Override
		public double getLoss(double expected, double actual, double... params) {
			double workingEps = params.length == 1 ? params[0] : EPSILON;
			return Math.abs(expected - actual) < workingEps ? 0.0 : 1.0;
		}
	},

	ABSOLUTE{
		@Override
		public double getLoss(double expected, double actual, double... params) {
			return Math.abs(expected - actual);
		}
	},
	
	 SQUARED{
		@Override
		public double getLoss(double expected, double actual, double... params) {
			double diff = expected - actual;
			return diff*diff;
		}
	};
	
	private static final double EPSILON = 1e-7; //A safe, general epsilon
	
	/**
	 * The loss measured on the outputs of two functions is always going to be
	 * non-negative though not necessarily with an upper bound of one.
	 * 
	 * @param expected true value of the function
	 * @param actual measured value of the function
	 * @param params optional parameters to the specific loss function
	 * @return the measured difference between the two given values
	 */
	public abstract double getLoss(double expected, double actual, double... params);
}
