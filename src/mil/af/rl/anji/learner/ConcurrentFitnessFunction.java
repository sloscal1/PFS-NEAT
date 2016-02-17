package mil.af.rl.anji.learner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import mil.af.rl.predictive.PFSInfo;
import mil.af.rl.predictive.SampleContainer;
import mil.af.rl.predictive.SubspaceIdentification;

import org.jgap.BulkFitnessFunction;
import org.jgap.Chromosome;

import com.anji.imaging.IdentifyImageFitnessFunction;
import com.anji.integration.ActivatorTranscriber;
import com.anji.util.Configurable;
import com.anji.util.Properties;

/**
 * This class implements a Fitness Function for the ANJI platform that enables learning over the neural networks.
 * It creates an instance of an RL_Learner object which it obtains from the properties file.  The fitness of the network
 * is determined by the RL_Learner
 *  
 * @author Steven Loscalzo
 */
public class ConcurrentFitnessFunction implements BulkFitnessFunction, Configurable {
	/** Unique version ID */
	private static final long serialVersionUID = 7490326948243431700L;
	/** The learner class to use (i.e. mil.af.rl.anji.Evolver) */
	public final static String LEARNER_CLASS_KEY = "learner";
	/** The fitness at which this experiment will terminate */
	public final static String THRESHOLD_KEY = "fitness.threshold";
	/** The number of threads to attempt to use in this experiment */
	public static final String NUM_THREADS_KEY = "fitness.numThreads";
	/** The activator transcriber class to use (needed in a prior implementation of PFS) */
	public static final String ACT_TRANS_KEY = "fitness.activator_transcriber";
	
	/** Must be positive */
	private int fitnessThreshold = 1;
	/** Used to determine how many jobs it might be safe to start up at one time,
	 *  storing samples can tax RAM! */
	private long contextMemUsage;
	/** The shared PFS management data structure */
	private PFSInfo info;
	
	/** Here's a threadpool to evaluate the chromosomes */
	private ExecutorService threadPool = Executors.newCachedThreadPool();
	/** The problem context to avoid making many environment objects */
	private BlockingQueue<Context> contexts = new LinkedBlockingQueue<>();
	
	/**
	 * @see com.anji.util.Configurable#init(com.anji.util.Properties)
	 */
	public void init( Properties props ) throws Exception {
		fitnessThreshold = props.getIntProperty(THRESHOLD_KEY);
		int numThreads = props.getIntProperty(NUM_THREADS_KEY, Runtime.getRuntime().availableProcessors()*4);

		int id = 0;
		if(contexts.size() != numThreads){
			synchronized(contexts){
				while(contexts.size() != numThreads)
					contexts.add(new Context((RL_Learner) props.newObjectProperty(LEARNER_CLASS_KEY),
							(ActivatorTranscriber) props.newObjectProperty(ACT_TRANS_KEY),
							id++));
			}
		}
		
		//For memory management concerns:
		//Max samples per evaluation * (num doubles per sample == s+a+s'+r) * bytes per double (8)) 
		contextMemUsage = props.getIntProperty(SampleContainer.MAX_SAMPLES, 1) * (2 * props.getIntProperty(SubspaceIdentification.FS_FEATURES_KEY, 0) + props.getIntProperty(SubspaceIdentification.FS_ACTIONS_KEY, 0) + 1) * 8;
	}
	
	/**
	 * Set the PFSInfo object that the evaluators will use to store samples.
	 * @param info
	 */
	public void setPFSInfo(PFSInfo info){
		this.info = info;
	}
	
	/**
	 * This class encapsulates the context that a task (chromosome) will execute
	 * in.  These can be reused from generation to generation.
	 * 
	 * @author sloscal1
	 *
	 */
	private class Context{
		private RL_Learner learner;
		private ActivatorTranscriber factory;
		private int id;
		
		public Context(RL_Learner learner, ActivatorTranscriber factory, int id){
			this.learner = learner;
			this.factory = factory;
			this.id = id;
		}
	}
	
	/**
	 * The task which will be evaluated.
	 * @author sloscal1
	 *
	 */
	private class EvaluationTask implements Callable<Void>{
		private Chromosome task;
		private Context context;
		
		public EvaluationTask(Context context, Chromosome task){
			this.context = context;
			this.task = task;
		}
		
		@Override
		public Void call() throws Exception {
			//Evaluate this chromosome
			double fitness  = 0;
			try{
				fitness = context.learner.evaluate(task, context.factory, info);
			}catch (Exception e){
				e.printStackTrace();
				System.exit(1);
			}
			//Free up a context to let another thread use it
			contexts.add(context);
			//Do the book-keeping
			task.setFitnessValue((int)fitness);
			evaluate(task, context.id);
			return null;
		}
	}
	
	/**
	 * @see org.jgap.BulkFitnessFunction#evaluate(java.util.List)
	 * @see IdentifyImageFitnessFunction#evaluate(Chromosome)
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public void evaluate( List genotypes ) {
		List<Chromosome> cGenotypes = genotypes;
		List<Future<Void>> futures = new ArrayList<Future<Void>>();
		//Figure out the number of contexts that can be supported in RAM now (up to the original size)
		//This is a worst case analysis
		List<Context> unsupportable = new ArrayList<Context>(contexts.size()-1); //Must have at least 1 thread!
		long freeBytes = Runtime.getRuntime().freeMemory();
		int supportable = (int)(freeBytes/contextMemUsage)/2; // /2 since buffer analysis may be slower than evaluation
		for(int i = 0; i < contexts.size() - supportable; ++i)
			try {
				unsupportable.add(contexts.take());
			} catch (InterruptedException e) {
				--i; //Try it again... should use a while loop
			}
		//Always risk having at least 1 context to make progress
		if(contexts.size() == 0)
			contexts.add(unsupportable.remove(unsupportable.size()-1));

		//Evaluate each chromosome
		for(Chromosome chrom : cGenotypes)
			try {
				futures.add(threadPool.submit(new EvaluationTask(contexts.take(), chrom)));
			} catch (InterruptedException e) {
				e.printStackTrace();
				//Just allow the loop to continue, it'll just wait again...
			}
		
		//Make sure all calculations have been completed before returning.
		for(int i = 0; i < futures.size(); ++i)
			try {
				futures.get(i).get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
				//Assume that the thread finished executing.
			}
		//Now make the other contexts available in case more RAM is available in the future:
		contexts.addAll(unsupportable);
	}

	public void evaluate(Chromosome c, int id) {
		//_DB_ 02/16/16  Doesn't matter for what we're doing with novelty
		//if(c.getClass().getSimpleName().equals("Chromosome")){ //_SL_ 6/27/11 Don't want to do this for the subtask chromosome
			StringBuilder sb = new StringBuilder(" Chromosome " + c.getId() + " Fitness=" + c.getFitnessValue() + " Primary Parent = " + c.getPrimaryParentId());
			sb.append("  Completed in context:" + id);
			System.out.println(sb.toString());
		//}
	}

	/**
	 * @see org.jgap.BulkFitnessFunction#getMaxFitnessValue()
	 */
	public int getMaxFitnessValue() {
		return ( fitnessThreshold );
	}

	/**
	 * Terminate the threadpool immediately instead of waiting for the idle timeout.
	 */
	public void shutdown() {
		threadPool.shutdownNow();
	}
}
