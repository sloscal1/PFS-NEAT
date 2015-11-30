package mil.af.rl.novelty;

import java.util.List;

public class NoveltyArchive {

	/**
	 * Put the given behavior vector into this archive, associated with the given id.
	 * 
	 * @param behaviorVector must not be null
	 * @param id must not be null
	 */
	public void put(double[] behaviorVector, Long id) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Retrieve the point associated with this id
	 * @param id
	 * @return null if the id is not found
	 */
	public double[] get(Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Return up to k nearest neighbors of the behavioral vector associated with
	 * the given id. If k > number of points in the archive, only the maximum number
	 * of available points will be returned.
	 * 
	 * @param k must be positive.
	 * @param id must not be null
	 * @return k behvaioral vectors associated with the k nearest neighbors of id
	 */
	public List<double[]> getKNN(int k, Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Request the archive update its long-term contents according to
	 * its specific policy. The last <code>size</code> puts are assumed
	 * to be short term adds and are subject for consideration to add to
	 * the long term store (or else are removed from this archive).
	 * @param size
	 */
	public void update(int size) {
		// TODO Auto-generated method stub
		
	}
}
