package mil.af.rl.anji.learner;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Scanner;

import org.jgap.BehaviorChromosome;
import org.jgap.Chromosome;

import com.anji.integration.ActivatorTranscriber;
import com.anji.util.Properties;

import mil.af.rl.anji.SerializableNetwork;
import mil.af.rl.predictive.PFSInfo;

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
	
	@Override
	public void init(Properties props) throws Exception {
		super.init(props);

		this.mainClass = props.getProperty(REMOTELEARNER_MAINCLASS_KEY);
		this.classPath = props.getProperty(REMOTELEARNER_CLASSPATH_KEY);
		this.propsFileName = props.getProperty(REMOTELEARNER_PROPS_NAME_KEY);
		
		/**
		 * This class simply loops on the child processes's error stream.
		 * If an error occurs, the program quits.
		 * @author sloscal1
		 *
		 */
		final class ErrorHandler implements Runnable{
			private InputStream is;
			private ErrorHandler(InputStream is){
				this.is = is;
			}
			
			@Override
			public void run() {
				Scanner scan = new Scanner(is);
				while(scan.hasNextLine()){
					System.err.println(scan.nextLine());
					if(!scan.hasNextLine())
						break;
				}
				scan.close();
				System.exit(1);
			}
		}
		
		ProcessBuilder pb = new ProcessBuilder("java", "-cp", classPath, mainClass, propsFileName);
		Process proc = pb.start();
		results = new ObjectInputStream(proc.getInputStream());
		new Thread(new ErrorHandler(proc.getErrorStream())).start();
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

		try {
			netWriter.writeObject((Long)25104141L);
			netWriter.writeObject(new SerializableNetwork(chrom));
			netWriter.flush();

			//Sim was started up in the init method, now has the network,
			//get the results
			fitness = (Integer)results.readObject();
			double[] behavior = (double[])results.readObject();
			//_SL_ 20160204: Don't like this solution, should avoid cast by changing the way
			//the vector gets passed around.
			if(chrom instanceof BehaviorChromosome)
				((BehaviorChromosome)chrom).setBehaviorVector(behavior);
				
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return fitness;
	}
}
