package mil.af.rl.novelty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.anji.util.Properties;

import mil.af.rl.anji.learner.RL_Learner;
import mil.af.rl.util.InstanceUtils;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.KDTree;
import weka.core.neighboursearch.NearestNeighbourSearch;

public class RandomSamplingNoveltyArchive implements NoveltyArchive{
	public static final String RANDOM_FRAC_KEY = "novelty_archive.rand_frac";
	
	/** The probability that a newly added point will be retained in the archive on an update */
	private double frac = 0.02;
	/** The source of randomness for the archive */
	private Random rand;
	
	/** A mapping from Chromosome id's to position in the data array */
	private Map<Long, Integer> vectorMap = new HashMap<>();
	/** The active behavior data array */
	private List<double[]> data = new ArrayList<>();
	/** The most recently added id's, not yet added to the permanent archive */
	private Set<Long> recent = new HashSet<>();
	/** The algorithm to actually obtain the NNs */
	private NearestNeighbourSearch searcher = new KDTree();
	/** The data format required by the algorithm */
	private Instances insts;
	/** Whether or not the data set has been altered (requiring NN algo rerun before query) */
	private boolean altered = false;
	
	/**
	 * Put the given behavior vector into this archive, associated with the given id.
	 * 
	 * @param behaviorVector must not be null
	 * @param id must not be null
	 */
	@Override
	public void put(double[] behaviorVector, Long id) {
		int pos = vectorMap.containsKey(id) ? vectorMap.get(id) : data.size();
		if(pos == data.size()){
			recent.add(id);
			vectorMap.put(id, pos);
		}
		if(pos == data.size()) data.add(null);
		data.set(pos, behaviorVector);
		altered = true;
	}

	/**
	 * Retrieve the point associated with this id
	 * @param id
	 * @return null if the id is not found
	 */
	@Override
	public double[] get(Long id) {
		return data.get(vectorMap.get(id));
	}

	/**
	 * Return up to k nearest neighbors of the behavioral vector associated with
	 * the given id. If k > number of points in the archive, only the maximum number
	 * of available points will be returned.
	 * 
	 * @param k must be positive.
	 * @param id must not be null
	 * @return k behavioral vectors associated with the k nearest neighbors of id
	 */
	@Override
	public List<double[]> getKNN(int k, Long id) {
		return getKNN(k, data.get(vectorMap.get(id)));
	}
	
	/**
	 * Return up to k nearest neighbors of the behavioral vector. 
	 * If k > number of points in the archive, only the maximum number
	 * of available points will be returned.
	 * 
	 * @param k must be positive.
	 * @param vector must not be null
	 * @return k behavioral vectors associated with the k nearest neighbors of id
	 */
	@Override
	public List<double[]> getKNN(int k, double[] vector) {
		//See if we need to rebuild the index...
		if(altered){
			insts = InstanceUtils.createInstances(data, false, null);
			try {
				searcher.setInstances(insts);
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
			altered = false;
		}
		
		//Need to turn the behavior vector into an Instance
		Instance newInst = new DenseInstance(insts.numAttributes());
		for(int i = 0; i < newInst.numAttributes(); ++i)
			newInst.setValue(i, vector[i]);
		newInst.setDataset(insts);
		
		//Find the nearest neighbors according to the last altered index
		Instances nns = null;
		try {
			nns = searcher.kNearestNeighbours(newInst, k);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		//Convert to a non-weka format:
		List<double[]> retVal = new ArrayList<>(k);
		for(int i = 0; i < nns.numInstances(); ++i)
			retVal.add(nns.get(i).toDoubleArray());
		
		return retVal;
	}

	/**
	 * Request the archive update its long-term contents according to
	 * its specific policy. The last <code>size</code> puts are assumed
	 * to be short term adds and are subject for consideration to add to
	 * the long term store (or else are removed from this archive).
	 * @param size
	 */
	@Override
	public void update(int size) {
		//Following a simple archive strategy right now, random:
		//First, see which recent members are to be removed
		//Keep track of their data indices..
		List<Integer> toRemove = new ArrayList<>(recent.size());
		for(Long id : recent)
			if(rand.nextDouble() > frac){
				toRemove.add(vectorMap.get(id));
				vectorMap.remove(id);
			}
		
		//Sort the indices to make it easier to remove
		Collections.sort(toRemove);
		
		//Go through and pull off the ones that aren't needed from the archive.
		for(int i = toRemove.size()-1; i >= 0; --i)
			data.remove(toRemove.get(i));
		if(!recent.isEmpty()){
			recent.clear();
			altered = true; //Assume that some points aren't involved anymore		
		}
		System.out.println("Archive size: "+data.size());
	}

	@Override
	public void init(Properties props) throws Exception {
		rand = new Random(props.getLongProperty(RL_Learner.LEARNER_RANDOM_SEED_KEY));
		frac = props.getDoubleProperty(RANDOM_FRAC_KEY);
	}
}
