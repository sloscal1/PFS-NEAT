package mil.af.rl.anji.learner;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jgap.Chromosome;

import com.anji.integration.ActivatorTranscriber;
import com.anji.util.Properties;

import mil.af.rl.anji.SerializableNetwork;
import mil.af.rl.predictive.ArraySample;
import mil.af.rl.predictive.EagerChromosomeSampleContainer;
import mil.af.rl.predictive.EagerChromosomeSampleContainer.ContainerInfo;
import mil.af.rl.predictive.PFSInfo;
import mil.af.rl.predictive.Sample;
import mil.af.rl.predictive.Sample.SampleType;
import mil.af.rl.predictive.SampleContainer;

/**
 * The remote learner expects to interact with another Java process to evaluate a Chromosome.
 * 
 * The process will begin execution when the learning contexts are started before the evolutionary
 * process begins. Then, in each chromosome evaluation call, the chromosome will be passed to the
 * other process via an ObjectOutputStream connected to that process's standard input, and
 * the evaluator will wait for two objects in reply: the first is fitness (Double) and the second
 * is a matrix of samples (double[][]). The second object may be null if feature selection is
 * not desired. Each row of the matrix represents one sample, and is orderd as <state, action, reward,
 * next state>. The state and next state portions are of length "stimulus.size" from the properties
 * file, and the action portion is of length "subspace.actions".
 * 
 * @author sloscal1
 *
 */
public class RemoteLearner extends RL_Learner {
	public static final String REMOTELEARNER_WORKING_DIR_KEY = "remotelearner.working_dir";
	public static final String REMOTELEARNER_CLASSPATH_KEY = "remotelearner.classpath";
	public static final String REMOTELEARNER_MAINCLASS_KEY = "remotelearner.mainclass";
	public static final String REMOTELEARNER_PROPS_NAME_KEY = "remotelearner.props_name";

	private String mainClass;
	private String classPath;
	private String propsFileName;
	private ObjectInputStream results;
	private ObjectOutputStream netWriter;
	private int stateBoundary;
	private int actionBoundary;
	private int rewardBoundary;
	
	@Override
	public void init(Properties props) throws Exception {
		super.init(props);

		this.mainClass = props.getProperty(REMOTELEARNER_MAINCLASS_KEY);
		this.classPath = props.getProperty(REMOTELEARNER_CLASSPATH_KEY);
		this.propsFileName = props.getProperty(REMOTELEARNER_PROPS_NAME_KEY);
		int numStateVariables = props.getIntProperty("stimulus.size");
		int numActionVariables = props.getIntProperty("subspace.actions");
		stateBoundary = numStateVariables;
		actionBoundary = stateBoundary+numActionVariables;
		rewardBoundary = actionBoundary+1;
		
		ProcessBuilder pb = new ProcessBuilder("java", "-cp", classPath, mainClass, propsFileName);
		pb.redirectErrorStream(true);
		Process proc = pb.start();
		results = new ObjectInputStream(proc.getInputStream());
		netWriter = new ObjectOutputStream(proc.getOutputStream());
		netWriter.flush();
	}

	@Override
	public double evaluate(Chromosome chrom, ActivatorTranscriber factory,
			PFSInfo info) {
		return run(chrom, factory, info);
	}

	@Override
	protected double run(Chromosome chrom, ActivatorTranscriber factory, PFSInfo info) {
		//The return value from this run
		double fitness = -1.0;
		double[][] samples = null;

		//Eager collection requires a bit of handling by the chromosome.
		SampleContainer old = info.getContainer(chrom);
		long eagerChromContId = -1L;
		List<Sample> newSamples = new ArrayList<>();
		if(info.isCollecting(chrom) && old instanceof EagerChromosomeSampleContainer){
			ContainerInfo cinfo = ((EagerChromosomeSampleContainer) old).getFreeContainer();
			eagerChromContId = cinfo.getId();
			info.setSampleContainer(chrom, cinfo.getSampleContainer());
		}

		try {
			netWriter.writeObject(new SerializableNetwork(chrom));
			netWriter.flush();

			//Sim was started up in the init method, now has the network,
			//get the results
			fitness = (Integer)results.readObject();
//			samples = (double[][])results.readObject();
//			if(samples != null && info.isCollecting(chrom)){
//				for(double[] sample : samples){
//					newSamples.add(new ArraySample(Arrays.copyOfRange(sample, 0, stateBoundary),
//							Arrays.copyOfRange(sample, stateBoundary, actionBoundary),
//							sample[actionBoundary],
//							Arrays.copyOfRange(sample, rewardBoundary, sample.length),
//							SampleType.UNKNOWN, 
//							chrom.getId().intValue()));
//				}
//			}
				
			

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Signal that it is time to evaluate these samples for more permanent storage
		if(info.isCollecting(chrom)){
			info.getContainer(chrom).addSamples(newSamples);
			if(old instanceof EagerChromosomeSampleContainer){
				((EagerChromosomeSampleContainer)old).evaluate(chrom.getId(), eagerChromContId, fitness);
				info.setSampleContainer(chrom, old);
			}
		}

		return fitness;
	}
}
