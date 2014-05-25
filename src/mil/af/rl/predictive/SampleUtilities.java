package mil.af.rl.predictive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import mil.af.rl.predictive.Sample;
import mil.af.rl.util.InstanceUtils;
import mil.af.rl.util.DoubleIndexPair;
import weka.core.Instance;

public class SampleUtilities {
	public enum SampleRelationship {
		REWARD{
			@Override
			public List<Double> convertToList(Sample s) {
				List<Double> retVal = new ArrayList<Double>(1);
				retVal.add(s.getReward());
				return retVal;
			}

			@Override
			public double[] convertToArray(Sample s) {
				return new double[]{s.getReward()};
			}
		},
		
		STATE_ONE_ACTION {
			@Override
			public List<Double> convertToList(Sample s) {
				Iterator<Double> state = s.getState();
				List<Double> retVal = new ArrayList<Double>();
				while(state.hasNext())
					retVal.add(state.next());
				retVal.add(s.getAction(response));
				return retVal;
			}

			@Override
			public double[] convertToArray(Sample s) {
				double[] soa = new double[s.getStateLength()+1];
				Iterator<Double> iter = s.getState();
				int i = 0;
				while(iter.hasNext())
					soa[i++] = iter.next();
				soa[soa.length-1] = s.getAction(response);
				return soa;
			}
		},
		
		STATE_ALL_ACTION {
			@Override
			public List<Double> convertToList(Sample s) {
				Iterator<Double> state = s.getStateAction();
				List<Double> retVal = new ArrayList<Double>();
				while(state.hasNext())
					retVal.add(state.next());
				return retVal;
			}

			@Override
			public double[] convertToArray(Sample s) {
				double[] saa = new double[s.getStateLength()+s.getActionLength()];
				Iterator<Double> iter = s.getStateAction();
				int i = 0;
				while(iter.hasNext())
					saa[i++] = iter.next();
				return saa;
			}
		},
		
		STATE_REWARD {
			@Override
			public List<Double> convertToList(Sample s) {
				Iterator<Double> state = s.getState();
				List<Double> retVal = new ArrayList<Double>();
				while(state.hasNext())
					retVal.add(state.next());
				retVal.add(s.getReward());
				return retVal;
			}

			@Override
			public double[] convertToArray(Sample s) {
				double[] sar = new double[s.getStateLength()+1];
				Iterator<Double> iter = s.getState();
				int i = 0;
				while(iter.hasNext())
					sar[i++] = iter.next();
				sar[sar.length-1] = s.getReward();
				return sar;
			}
		},
		
		STATE_ACTION_REWARD{
			@Override
			public List<Double> convertToList(Sample s) {
				Iterator<Double> sar = s.getStateActionReward();
				List<Double> retVal = new ArrayList<Double>();
				while(sar.hasNext())
					retVal.add(sar.next());
				return retVal;
			}

			@Override
			public double[] convertToArray(Sample s) {
				double[] sar = new double[s.getStateLength()+s.getActionLength()+1];
				Iterator<Double> iter = s.getStateAction();
				int i = 0;
				while(iter.hasNext())
					sar[i++] = iter.next();
				sar[sar.length-1] = s.getReward();
				return sar;
			}
		},
		
		DELTA_STATE_STATE {
			@Override
			public double[] convertToArray(Sample s) {
				Iterator<Double> state = s.getState();
				Iterator<Double> statePrime = s.getStatePrime();
				double[] retVal = new double[s.getStateLength() + 1];
				int i = 0;
				while(state.hasNext())
					retVal[i] = statePrime.next() - state.next();
				retVal[retVal.length-1] = s.getStatePrime(response) - s.getState(response);
				return retVal;
			}

			@Override
			public List<Double> convertToList(Sample s) {
				Iterator<Double> state = s.getState();
				Iterator<Double> statePrime = s.getStatePrime();
				List<Double> retVal = new ArrayList<Double>(s.getStateLength() + 1);
				while(state.hasNext())
					retVal.add(statePrime.next() - state.next());
				retVal.add(s.getStatePrime(response) - s.getState(response));
				return retVal;
			}

		}, 
		
		DELTA_STATE_ACTION {
			@Override
			public List<Double> convertToList(Sample s) {
				Iterator<Double> state = s.getState();
				Iterator<Double> statePrime = s.getStatePrime();
				List<Double> retVal = new ArrayList<Double>();
				while(state.hasNext()){
					retVal.add(statePrime.next() - state.next());
				}
				retVal.add(s.getAction(response));
				return retVal;
			}

			@Override
			public double[] convertToArray(Sample s) {
				double[] delta = new double[s.getStateLength()+1];
				Iterator<Double> iter = s.getState();
				Iterator<Double> iter2 = s.getStatePrime();
				int i = 0;
				while(iter.hasNext())
					delta[i++] = iter2.next() - iter.next();
				delta[delta.length-1] = s.getAction(response);
				return delta;
			}
		};
		
		protected int response = -1; // Negative one will break a program if not
										// set.

		public void setResonse(int response) {
			this.response = response;
		}

		public Instance convertToInstance(Sample s) {
			return InstanceUtils.instanceFromList(1.0, convertToList(s));
		}

		public abstract double[] convertToArray(Sample s);
		
		public abstract List<Double> convertToList(Sample s);
	}

	public static List<Double> convertToList(Iterator<Double> iter){
		List<Double> list = new ArrayList<Double>();
		while(iter.hasNext())
			list.add(iter.next());
		return list;
	}
	
	public static double[] convertToArray(Iterator<Double> iter){
		return listToArray(convertToList(iter));
	}
	
	private static double[] listToArray(List<Double> list){
		double[] arr = new double[list.size()];
		for(int i = 0; i < list.size(); ++i)
			arr[i] = list.get(i);
		return arr;
	}
	
	/**
	 * Converts the given collection of samples to a double matrix according to
	 * the SampleRelationship. The response will always be in the last column of
	 * the matrix (according to how the SampleRelationships are currently
	 * defined).
	 * 
	 * @param rel
	 * @param samples
	 * @return
	 */
	public static double[][] convertSamplesToMatrix(SampleRelationship rel,
			Collection<Sample> samples) {
		double[][] retVal = new double[samples.size()][];

		int i = 0;
		for (Sample s : samples)
			retVal[i++] = rel.convertToArray(s);

		return retVal;
	}

	public static void roundTo6Decimals(double[] sample){
		for(int j = 0; j < sample.length; ++j)
			sample[j] = Math.round(sample[j]*1000000)/1000000.0;
	}
	
	public static void roundTo6Decimals(List<Double> sample){
		for(int j = 0; j < sample.size(); ++j)
			sample.set(j, Math.round(sample.get(j)*1000000)/1000000.0);
	}
	
	public static void roundTo6Decimals(double[][] sampleData) {
		for(int i=0; i<sampleData.length; i++) {
			for(int j=0; j<sampleData[i].length; j++) {
				sampleData[i][j] = Math.round(sampleData[i][j]*1000000)/1000000.0;
			}
		}
	}
	/**
	 * Performs a simple binning of the response values. It will result in the
	 * values becoming integers in the range [0,numVals-1].
	 * 
	 * @param sampleData
	 * @param numVals
	 */
	public static void discretizeResponse(double[][] sampleData, int numVals) {
		// Figure out the starting range
		int r = sampleData[0].length - 1;

		// Get the min and max
		DoubleIndexPair<Double, Double> minMax = new DoubleIndexPair<Double, Double>(
				sampleData[0][r], sampleData[0][r]);
		for (int i = 1; i < sampleData.length; ++i)
			if (sampleData[i][r] < minMax.getElement1())
				minMax.setElement1(sampleData[i][r]);
			else if (sampleData[i][r] > minMax.getElement2())
				minMax.setElement2(sampleData[i][r]);

		// Scale everything to the bin range:
		double range = minMax.getElement2() - minMax.getElement1();
		for (int i = 0; i < sampleData.length; ++i) {
			double val = (sampleData[i][r] - minMax.getElement1()) / range
					* numVals;
			// The max will be numVals, violating the max bin constraint:
			if (val >= numVals)
				val = numVals - 1; // It will be truncated to an int anyway
			sampleData[i][r] = val;
		}
	}

	/**
	 * Performs a simple binning of the response values. It will result in the
	 * values becoming integers in the range [0,numVals-1].
	 * 
	 * @param sampleData
	 * @param numVals
	 */
	public static void discretizeAndClip(double[][] sampleData,
			int numPredVals, int numRespVals, int divInd) {
		// Figure out the starting range
		int numVals = numPredVals;
		for (int r = 0; r < sampleData[0].length; r++) {
			if (r == divInd)
				numVals = numRespVals;
			// Get the min and max
			ArrayList<Double> maxs = new ArrayList<Double>();
			ArrayList<Double> mins = new ArrayList<Double>();
			for (int i = 0; i < sampleData.length; ++i) {
				maxs.add(sampleData[i][r]);
				mins.add(sampleData[i][r]);
				if (i >= 5) {
					Collections.sort(maxs);
					Collections.sort(mins);
					maxs.remove(0);
					mins.remove(5);
				}
			}

			// Scale everything to the bin range:
			double range = maxs.get(0) - mins.get(4);
			for (int i = 0; i < sampleData.length; ++i) {
				double val = (sampleData[i][r] - mins.get(4)) / range * numVals;
				// The max will be numVals, violating the max bin constraint:
				if (val >= numVals)
					val = numVals - 1; // It will be truncated to an int anyway
				if (val < 0)
					val = 0;
				sampleData[i][r] = val;
			}
		}
	}

	/**
	 * Performs a simple binning of the response values. It will result in the
	 * values becoming integers in the range [0,numVals-1].
	 * 
	 * @param sampleData
	 * @param numVals
	 */
	public static void equalFreqBinning(double[][] sampleData, int numPredVals,
			int numRespVals, int divInd) {
		if (sampleData.length > 0) {
			double[][] inds = new double[sampleData[0].length][Math.max(
					numPredVals, numRespVals)];
			int num = numPredVals;
			for (int r = 0; r < sampleData[0].length; r++) {
				if (r == divInd) {
					num = numRespVals;
				}
				double[] singleData = new double[sampleData.length];
				for (int i = 0; i < sampleData.length; i++) {
					singleData[i] = sampleData[i][r];

				}
				Arrays.sort(singleData);
				for (int i = 0; i < num; i++) {
					inds[r][i] = singleData[i * singleData.length / num];
				}
			}
			for (double[] sample : sampleData) {
				for (int i = 0; i < divInd; i++) {
					int val = 1;
					while (val < numPredVals && inds[i][val] < sample[i]) {
						val++;
					}
					sample[i] = val - 1.0;
				}
				for (int i = divInd; i < sample.length; i++) {
					int val = 1;
					while (val < numRespVals && inds[i][val] < sample[i]) {
						val++;
					}
					sample[i] = val - 1.0;
				}

			}
		}
	}
}
