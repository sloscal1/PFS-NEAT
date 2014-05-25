package mil.af.rl.util;

/**
 * This class contains various useful similarity measures and will grow as
 * new measures are encountered.
 * @author Steven Loscalzo
 * @date   7/1/08  Initial draft with Pearson Correlation and Euclidean distance
 * 				   (previously implemented in other files).
 * @date   1/13/09 Added cohesion and separation measures
 */
public class SimMeasures {	
	/**
	 * This method takes two arrays of doubles and computes the pearson correlation
	 * coefficient between the two arrays.  They must be the same size for the
	 * computation to execute, if they are not the method will return Double.NaN.
	 * 
	 * @param f1 The values of one feature to compare with
	 * @param f2 The values of another feature to compare with
	 * @return the pearson correlation coefficient between f1 and f2
	 */
	public static double pearson(double[] f1, double[] f2){
		double pearson = Double.NaN;
		//Verify that the lengths are identical
		if(f1 != null && f2 != null && f1.length == f2.length){
			//this version of pearson taken illustrated in
			//http://davidmlane.com/hyperstat/A51911.html
			pearson = 0.0;
			double x = 0.0;
			double y = 0.0;
			double xy = 0.0;
			double xS = 0.0;
			double yS = 0.0;
			
			for(int i = 0; i < f1.length; ++i){
				double xi = f1[i];
				double yi = f2[i];
				xy += xi*yi;
				x += xi;
				y += yi;
				xS += xi*xi;
				yS += yi*yi;
			}
			pearson = xy - x*y/f1.length;
			pearson /= Math.sqrt((xS-x*x/f1.length)
					*(yS-y*y/f1.length));
		}
		return pearson;
	}
	
	/**
	 * This method computes the euclidean distance between two equal sizes
	 * arrays of doubles.  If the arrays do not have equivalent length then this
	 * method will return Double.NaN.
	 * = Sqrt(Sum(i = 0 -> f1.length) (f1i - f2i)^2)
	 * @param f1 the values of one feature to compare with
	 * @param f2 the values of the other feature to compare with
	 * @return the euclidean distance between the two arrays
	 */
	public static double euclidean(double[] f1, double[] f2){
		double euclidean = Double.NaN;
		//Verify that the arrays are not null
		if(f1 != null && f2 != null && f1.length == f2.length){
			euclidean = 0.0;
			//Add up all of the values (squared)
			for(int i = 0; i < f1.length; ++i)
				euclidean += (f1[i] - f2[i])*(f1[i] - f2[i]);
			//Take the square root
			euclidean = Math.sqrt(euclidean);
		}
		return euclidean;
	}
	
	/**
	 * This method computes the euclidean distance between two equal sizes
	 * arrays of doubles.  If the arrays do not have equivalent length then this
	 * method will return Double.NaN.
	 * = Sqrt(Sum(i = 0 -> f1.length) (f1i - f2i)^2)
	 * @param f1 the values of one feature to compare with
	 * @param f2 the values of the other feature to compare with
	 * @return the euclidean distance between the two arrays
	 */
	public static double euclidean(double[] f1, Double[] f2){
		double[] trans = new double [f2.length];
		//Just translate the Double array into a double array and use the
		//other euclidean method.
		for(int i = 0; i < trans.length; ++i)
			trans[i] = f2[i];
		return euclidean(f1, trans);
	}
	
	/**
	 * The Kolmogorov-Smirnov statistic.  Given two distributions, this statistics finds the greatest difference
	 * between the two distributions.  Here it is assumed that the CDF is represented by equally sized arrays where
	 * each cell in the array covers the same portion of the underlying random variable.
	 * 
	 * @param dist1
	 * @param dist2
	 * @return
	 * @throws IllegalArgumentException if the arguments do not have the same length.
	 */
	public static double Kolmogorov_Smirnov(double[] dist1, double[] dist2){
		if(dist1.length != dist2.length)
			throw new IllegalArgumentException("Distrubtion bins must be the same.");
		double ks = 0.0;
		for(int i = 0; i < dist1.length; ++i){
			double diff = Math.abs(dist1[i]-dist2[i]);
			if(diff > ks)
				ks = diff;
		}
		return ks;
	}
}
