package mil.af.rl.predictive;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import mil.af.rl.util.DoubleIndexPair;
import mil.af.rl.util.SingleIndexPair;

import com.anji.util.Configurable;
import com.anji.util.Properties;

/**
 * This class works with mil.af.rl.progressive.learners.Evolver and the related
 * NEAT learner classes to allow online sample collection without too much time
 * overhead.  Instead of replaying top performing chromosomes once known to get
 * samples, all samples are stored until it is seen that they are not top performers,
 * at which point they are discarded.  This is done in the background of learning,
 * so there is a lot of additional object creation but very little additional
 * overhead.
 * 
 * Useful when evaluation time is critical (such as with the double pole balance).
 * @author sloscal1
 *
 */
public class EagerChromosomeSampleContainer implements SampleContainer, Configurable {
	/** The top k chromosomes to use for sample collection */
	public static final String EAGER_NUM_CHROMS = "container.eager.k";
	/** The underlying sample containers that are to be used for actual collection */
	public static final String EAGER_BASE_CLASS = "container.eager.base.class";
	public static final String EAGER_TEMP_CLASS = "container.eager.temp.class";
	
	/** The base sample container */
	private SampleContainer base;
	/** The active buffers that have yet to be evaluated */
	private Map<Long, SampleContainer> buffers = new HashMap<>();
	/** The next free Id to use for the buffers data structure */
	private Long freeId;
	/** The top k sample collections that we are aware of so far */
	private LinkedList<SingleIndexPair<DoubleIndexPair<Double, Long>, Collection<Sample>>> topK = new LinkedList<>();
	/** Tasks that have been submitted for processing, used to make sure all
	 * samples have been processed before any samples are returned from this structure. */
	private List<Future<?>> pending = new ArrayList<Future<?>>(100);
	/** The number of sample collections to maintain */
	private int k;
	/** Threadpool to maintain buffers while they're being executed */
	private ExecutorService threadPool = Executors.newFixedThreadPool(2);
	private Properties props;
	
//	private static PrintWriter debug;
//	static {
//		try {
//			debug = new PrintWriter(new BufferedWriter(new FileWriter("debug.txt")));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * Create a new sample container which wraps base.  If the additional methods
	 * exclusive to this class are not used, then it performs exactly the same function
	 * as the base sample container.
	 * 
	 * @param base must not be null
	 * @param props must not be null
	 * @param k must be positive
	 */
	public EagerChromosomeSampleContainer(SampleContainer base, Properties props, int k){
		if(base == null || props == null || k <= 0)
			throw new IllegalArgumentException("SampleContainer and Properties must not be null, k must be positive.");

		this.base = base;
		this.k = k;
		this.props = props;
	}
	
	public EagerChromosomeSampleContainer(){}
	
	@Override
	public void init(Properties props) throws Exception {
		this.props = props;
		this.k = props.getIntProperty(EAGER_NUM_CHROMS);
		this.base = (SampleContainer)Class.forName(props.getProperty(EAGER_BASE_CLASS)).newInstance();
		if(base instanceof Configurable)
			((Configurable)base).init(props);
		if(k <= 0)
			throw new IllegalArgumentException(EAGER_NUM_CHROMS+" must be positive.");
	}

	@Override
	public void clearSamples() {
		base.clearSamples();
	}

	@Override
	public void addSample(Sample sample) {
		base.addSample(sample);
	}

	@Override
	public void addSamples(Collection<Sample> samples) {
		base.addSamples(samples);
	}

	@Override
	public void addSamples(Sample[] samples) {
		base.addSamples(samples);
	}

	@Override
	public Collection<Sample> getSamples() {
		return base.getSamples();
	}

	/**
	 * Get the base container
	 * @return will not be null
	 */
	public SampleContainer getBase(){
		return base;
	}

	/**
	 * Clear all temporary storage used by this class to determine which
	 * samples get added to base.  The samples in base are not changed by
	 * calling this method, they must be cleared separately by calling
	 * clearSamples().
	 */
	public void clearBuffers(){
		//need to make sure that all submitted tasks have completed.
		for(Future<?> f : pending)
			try {
				if(f != null) f.get();
			} catch (InterruptedException | ExecutionException e) {
				//Oh well... just move on without these results.
			}
		pending.clear();
		topK.clear();
		buffers.clear();
		freeId = 0L;
	}

	/**
	 * Triggers this structure to commit the samples found in the top k
	 * buffers to the base sample container.
	 */
	public void aggregateSamples(){
		//need to make sure that all submitted tasks have completed.
		for(Future<?> f : pending)
			try {
				if(f != null) f.get();
			} catch (InterruptedException | ExecutionException e) {
				//Oh well... just move on without these results.
			}

		synchronized(topK){
			System.out.println("WINNERS: ");
			for(SingleIndexPair<DoubleIndexPair<Double, Long>, Collection<Sample>> pair : topK){
				System.out.print(pair.getElement1().getElement1()+"/"+pair.getElement1().getElement2()+" ");
				base.addSamples(pair.getElement2());

			}
			System.out.println();
		}
	}

	@Override
	public void printSamples(String fileName) throws IOException {
		base.printSamples(fileName);
	}

	/**
	 * Get a buffer container.  Samples placed in the container by using the
	 * add methods do not get committed to base without first calling
	 * evaluate and aggregateSamples.
	 * 
	 * @return a new sample container to buffer samples in.
	 */
	public ContainerInfo getFreeContainer(){
		SampleContainer container = null;
		try {
			container = (SampleContainer)Class.forName(props.getProperty(EAGER_TEMP_CLASS)).newInstance();
		} catch (InstantiationException e1) {
			e1.printStackTrace();	
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		} finally{
			if(container == null)
				System.exit(1);
		}
		
		if(container instanceof Configurable)
			try {
				synchronized(props){
					int samps = props.getIntProperty(SampleContainer.MAX_SAMPLES);
					props.setProperty(SampleContainer.MAX_SAMPLES, ""+10000);
					((Configurable)container).init(props);
					props.setProperty(SampleContainer.MAX_SAMPLES, ""+samps);
				}
			} catch (Exception e) {
				//Shouldn't be any problems here.
				e.printStackTrace();
				System.exit(0);
			}
		Long id;
		synchronized(buffers){
			//Previously synched on freeId and did a dangerous id assignment
			while(buffers.containsKey(freeId))
				++freeId;
			id = freeId;
			buffers.put(id, container);
		}

		return new ContainerInfo(container, id);
	}

	/**
	 * Make a determination whether or not to keep buffer corresponding to id
	 * or to remove it from consideration.  Uses value to make this determination.
	 * This method does not cause base to be altered.
	 * 
	 * @param id
	 * @param value
	 */
	public void evaluate(long chromId, long contId, double value){
		pending.add(threadPool.submit(new BufferInsertion(chromId, contId, value)));
	}

	/**
	 * This class is the strategy that is followed to determine if a buffer
	 * will be placed in the top k buffers. Right now, it is simply done based
	 * on increasing value (of the evaluate method).
	 * 
	 * @author sloscal1
	 *
	 */
	private class BufferInsertion implements Callable<Void>{
		private long chromId;
		private long contId;
		private double value;
		
		private BufferInsertion(long chromId, long contId, double value){
			this.chromId = chromId;
			this.contId = contId;
			this.value = value;
		}

		public Void call(){
			synchronized (topK){
				//TODO This is debug code:
//				debug.println(buffers.get(contId).getSamples().iterator().next());
				//Figure out where this chromosome should be inserted by value:
				int i = topK.size() - 1;
				while(i >= 0 && value > topK.get(i).getElement1().getElement1())
					--i;
				//Now see if there are ties, push it forward based on chromId
				while(i >= 0 
						&& Math.abs(topK.get(i).getElement1().getElement1() - value) < 1e-5 
						&& chromId < topK.get(i).getElement1().getElement2())
					--i;
				//(i+1) is the insertion point
				//Figure out where the previous value of this chromosome is located:
				int chromPrevBest = 0;
				while(chromPrevBest < topK.size() && topK.get(chromPrevBest).getElement1().getElement2() != chromId)
					++chromPrevBest;
				if(chromPrevBest == topK.size())
					chromPrevBest = k;
				
				//If the insertion of this element is better than the previous best
				//(and implicitly, in the top k since cPB <= k)
				if(i+1 <= chromPrevBest){
					//remove the previous best
					if(chromPrevBest < topK.size())
						topK.remove(chromPrevBest);
					//Add the new value
					topK.add(i+1, new SingleIndexPair<>(new DoubleIndexPair<>(value, chromId), buffers.get(contId).getSamples()));
					//Verify that we keep topK <= k
					if(topK.size() > k)
						topK.removeLast();
				}
				buffers.remove(contId);
			}
			
			return null;
		}
	}

	public class ContainerInfo{
		private SampleContainer cont;
		private Long id;

		private ContainerInfo(SampleContainer cont, Long id){
			this.cont = cont;
			this.id = id;
		}

		public SampleContainer getSampleContainer(){
			return cont;
		}

		public Long getId(){
			return id;
		}
	}

	@Override
	public void cullSamples() {
		base.cullSamples();		
	}

	public void shutdown() {
		if(threadPool != null && !threadPool.isShutdown())
			threadPool.shutdown();		
	}
}