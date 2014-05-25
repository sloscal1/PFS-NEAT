package mil.af.rl.util;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ListUtils {
	
	/**
	 * Get the String representation of the contents of this list,
	 * surrounded by [ ]. 
	 * @param list the list of ints to print
	 * @return the String representation of the list
	 */
	public static String listToString(int[] list){
		StringBuffer buff = new StringBuffer("[");
		for(int i = 0; i < list.length-1; ++i)
			buff.append(list[i]+", ");
		buff.append(list[list.length-1]+"]");
		return buff.toString();
	}
	
	/**
	 * Get the String representation of the contents of this list,
	 * surrounded by [ ].   Each of the doubles will be formatted to
	 * 3 decimal places.
	 * @param list the list of doubles to print
	 * @return the String representation of the list
	 */
	public static String listToString(double[] list){
		DecimalFormat df = new DecimalFormat("#.###");
		StringBuffer buff = new StringBuffer("[");
		for(int i = 0; i < list.length-1; ++i)
			buff.append(df.format(list[i])+", ");
		buff.append(df.format(list[list.length-1])+"]");
		return buff.toString();
	}
	
	
	/**
	 * Returns the maximum double in the list of doubles.  Assumes that
	 * list is not null.
	 * @param list
	 * @return
	 */
	public static double maximum(double[] list){
		double max = list[0];
		for(int i = 1; i < list.length; ++i)
			if(list[i] > max)
				max = list[i];
		return max;
	}
	
	public static double minimum(double[] list){
		double min = list[0];
		for(int i = 1; i < list.length; ++i)
			if(list[i] < min)
				min = list[i];
		return min;
	}
	
	public static int minimum(int[] list){
		int min = list[0];
		for(int i = 1; i < list.length; ++i)
			if(list[i] < min)
				min = list[i];
		return min;
	}
	
	/**
	 * Returns the maximum int in the list of ints.  Assumes that
	 * list is not null.
	 * @param list
	 * @return
	 */
	public static int maximum(int[] list){
		int max = list[0];
		for(int i = 1; i < list.length; ++i)
			if(list[i] > max)
				max = list[i];
		return max;
	}
	
	
	/**
	 * This method computes the standard deviation of an array of doubles,
	 * given the average.
	 * @param avg The average of ds
	 * @param ds  The list of numbers
	 * @return the standard deviation of ds
	 */
	public static double computeStdev(double avg, double[] ds) {
		double sum = 0;
		for(double n : ds){
			double diff = n - avg;
			sum += diff * diff;
		}
		sum /= ds.length-1;
		return Math.sqrt(sum);
	}

	/**
	 * This method computes the standard deviation of an array of ints,
	 * given the average.
	 * @param avg The average of ds
	 * @param ds  The list of numbers
	 * @return the standard deviation of ds
	 */
	public static double computeStdev(double avg, int[] ds) {
		double sum = 0;
		for(int n : ds){
			double diff = n - avg;
			sum += diff * diff;
		}
		sum /= ds.length-1;
		return Math.sqrt(sum);
	}

	public static double computeStdev(double avg, List<? extends Number> fs) {
		double sum = 0;
		for(Number d : fs){
			double diff = avg - d.doubleValue();
			sum += diff * diff;
		}
		sum /= fs.size() - 1;
		return Math.sqrt(sum);
	}

	public static double computeMean(List<? extends Number> fs) {
		Double sum = 0.0;
		for(Number d : fs)
			sum += d.doubleValue();
		return sum/fs.size();
	}

	/**
	 * This method computes the average of a list of doubles.
	 * @param ds the list of numbers
	 * @return the average of ds
	 */
	public static double computeMean(double[] ds) {
		double d = 0;
		for(double n : ds)
			d += n;
		return d/ds.length;
		
	}

	
	/**
	 * This method computes the average of a list of ints.
	 * @param ds the list of numbers
	 * @return the average of ds
	 */
	public static double computeMean(int[] ds) {
		double d = 0;
		for(int n : ds)
			d += n;
		return d/ds.length;
	}

	/**
	 * This method finds the location of the first minimum of list
	 * @param list a list of doubles
	 * @return the location of the first occurence of the minimum element
	 */
	public static int minLocation(double[] list) {
		int location = 0;
		for(int i = 1; i < list.length; ++i)
			if(list[i] < list[location])
				location = i;
		
		return location;
	}
	
	/**
	 * This method finds the location of the first minimum of list
	 * @param list a list of doubles
	 * @return the location of the first occurence of the minimum element
	 */
	public static int maxLocation(double[] list) {
		int location = 0;
		for(int i = 1; i < list.length; ++i)
			if(list[i] > list[location])
				location = i;
		
		return location;
	}

	/**
	 * This method finds the location of the first minimum of list
	 * @param list a list of doubles
	 * @return the location of the first occurence of the minimum element
	 */
	public static int maxLocation(int[] list) {
		int location = 0;
		for(int i = 1; i < list.length; ++i)
			if(list[i] > list[location])
				location = i;
		
		return location;
	}
	
	/**
	 * This method finds the largest difference between numbers in the same cell
	 * of the two lists and returns the location where that difference occurs.
	 * The lists must be the same size.
	 * @param list1 
	 * @param list2
	 * @return the location of the largest difference between values from the same
	 * cell in the two lists
	 */
	public static int maxDiffLocation(double[] list1, double[] list2) {
		int location = 0;
		double val = (list1[location] - list2[location]);
		for(int i = 0; i < list1.length; ++i){
			if(Math.abs(list1[i] - list2[i]) > val){
				location = i;
				val = Math.abs(list1[i] - list2[i]);
			}
		}
		return location;
	}

	/**
	 * This method computes the median from a list of values and returns it.
	 * If values.length is odd then the single middle element will be returned.
	 * If values.length is even then the mean of the 2 central values will be returned.
	 * This method assumes that the length of values is positive.
	 * @param values the values to select the median from. 
	 * @return the median of values
	 */
	public static double computeMedian(double[] values) {
		double[] v = Arrays.copyOf(values, values.length);
		Arrays.sort(v); //Fastest way to get the median
		double median = v[v.length/2];
		//The median is the average of the central 2 if length is even
		if(v.length % 2 == 0){
			median += v[v.length/2-1];
			median /= 2;
		}
		return median;
	}
	
	/**
	 * This method computes the median from a list of values and returns it.
	 * If values.length is odd then the single middle element will be returned.
	 * If values.length is even then the mean of the 2 central values will be returned.
	 * This method assumes that the length of values is positive.
	 * @param values the values to select the median from. 
	 * @return the median of values
	 */
	public static double computeMedian(List<Double> values) {
		Double[] v = values.toArray(new Double[]{});
		Arrays.sort(v); //Fastest way to get the median
		double median = v[v.length/2];
		//The median is the average of the central 2 if length is even
		if(v.length % 2 == 0){
			median += v[v.length/2-1];
			median /= 2;
		}
		return median;
	}
	
	/**
	 * This method takes the Ln vector norm of the vector according to the formula:
	 * Norm = [E(i=0 to vector.length) |vector[i]|^n]^(1.0/n)
	 * @param vector
	 * @param n
	 * @return the vector norm of the given vector to the nth degree
	 */
	public static double norm(double[] vector, int n){
		double norm = 0.0;
		//Go through each component
		for(int i = 0; i < vector.length; ++i){
			double inner = 1.0;
			double comp = Math.abs(vector[i]);
			//Get the appropriate power of the component
			for(int dim = 0; dim < n; ++dim)
				inner *= comp;
			//Add the |comp|^n to our sum
			norm += inner;
		}
		//Take the appropriate root, don't waste the method call if
		//n is 1.
		if(n != 1)
			norm = Math.pow(norm, 1.0/n);
		return norm;
	}
	
	/**
	 * This method subtracts the second array from the first array and returns
	 * the difference in a new array.
	 * It is assumed that first and second have the same dimensionality.
	 * @param first
	 * @param second
	 * @return
	 */
	public static double[] sub(double[] first, double[] second){
		double[] res = new double[first.length];
		for(int i = 0; i < first.length; ++i)
			res[i] = first[i] - second[i];
		return res;
	}
	
	/**
	 * This method multiplies each component of the vector by the given scalar
	 * and returns the result in a new array.
	 * @param vector
	 * @param scalar
	 * @return
	 */
	public static double[] scalarMult(double[] vector, double scalar){
		double[] res = new double[vector.length];
		for(int i = 0; i < vector.length; ++i)
			res[i] = vector[i]*scalar;
		return res;
	}

	/**
	 * This method adds the second array and first array and returns the sum in
	 * a new array.
	 * It is assumed that first and second are the same size.
	 * @param first
	 * @param second
	 * @return
	 */
	public static double[] add(double[] first, double[] second) {
		double[] sum = new double[first.length];
		for(int i = 0; i < first.length; ++i)
			sum[i] = first[i] + second[i]; 
		return sum;
	}

	/**
	 * This method divides each component in the numerator by its corresponding
	 * component in the denominator.  It is assumed that numerator and denominator
	 * are the same size. The quotient is returned in a new array.
	 * @param numerator
	 * @param denominator
	 * @return
	 */
	public static double[] divide(double[] numerator, double[] denominator) {
		double[] quotient = new double[numerator.length];
		for(int i = 0; i < numerator.length; ++i)
			quotient[i] = numerator[i]/denominator[i];
		return quotient;
	}

	public static String arrayToString(int[] subset) {
		StringBuilder sb = new StringBuilder("[");
		for(int i = 0; i < subset.length-1; ++i)
			sb.append(subset[i]+",");
		sb.append(subset[subset.length-1]+"]");
		return sb.toString();
	}
	
	public static String arrayToString(double[] subset) {
		StringBuilder sb = new StringBuilder("[");
		for(int i = 0; i < subset.length-1; ++i)
			sb.append(subset[i]+",");
		sb.append(subset[subset.length-1]+"]");
		return sb.toString();
	}

	public static String arrayToString(Object[] subset) {
		StringBuilder sb = new StringBuilder("[");
		for(int i = 0; i < subset.length-1; ++i)
			sb.append(subset[i].toString()+",");
		sb.append(subset[subset.length-1].toString()+"]");
		return sb.toString();
	}

	/**
	 * This method creates a uniform array of value, the array has length length.
	 * It is assumed that length is non-negative.
	 * @param value
	 * @param length
	 * @return
	 */
	public static double[] constantVect(int value, int length) {
		double[] ret = new double[length];
		for(int i = 0; i < length; ++i)
			ret[i] = value;
		return ret;
	}

	/**
	 * For lists of Future results that we are uninterested in, act as a Barrier
	 * and attempt to get all futures. Exceptions will be silently ignored.
	 * 
	 * @param res must not be null.
	 */
	public static void waitForAll(List<Future<Void>> res) {
		for(Future<?> f : res)
			try {	
				f.get();
			} catch (InterruptedException | ExecutionException e) {
				// Don't worry about it.
			}
	}

	public static double computeVariance(double mean, List<Double> values) {
		double variance = 0.0;
		for(Double d : values)
			variance += (d - mean)*(d - mean);

		return variance/values.size();
	}
}

