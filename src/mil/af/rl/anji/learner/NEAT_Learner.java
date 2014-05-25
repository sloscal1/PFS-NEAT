package mil.af.rl.anji.learner;

import mil.af.rl.predictive.PFSInfo;

import org.jgap.Chromosome;

import com.anji.integration.Activator;
import com.anji.integration.ActivatorTranscriber;
import com.anji.util.Configurable;

/**
 * This class evaluates a policy represented by a NEAT chromosome. This class should
 * be the learner when using the ProgressiveFeatureSelectionFramework code with NEAT or FS-NEAT.
 * 
 * @author Robert Wright
 * @author Steven Loscalzo modified to accept PFSInfo due to interface change (object is unused in this code)
 *
 */
public class NEAT_Learner extends RL_Learner implements Configurable
{


	/**
	 * This method performs a single evaluation
	 * @param chrom The chromosome for the NN
	 * @param factory The factory for building the NN from the chromosome
	 * @return The fitness
	 */
	protected double run(Chromosome chrom, ActivatorTranscriber factory, PFSInfo info){
		int failures = 0;
		int numUpdates = 0;
		double bestRun = 0;  //Fitness of the best run
		double[] response = null;
		double[] actions = null;

		//BUILD the NN 
		Activator activator = null;
		try {		
			activator = factory.newActivator( chrom );
			//--- Iterate through the action-learn loop. ---
			while (!problem.inGoalState() && failures < MAX_FAILURES){			

				//--- Execute the action. ---
				double[] netInput = problem.getNetInput();
				//				double[] state = problem.getState();
				try{
					response = activator.next(netInput);
				}catch(Exception e){
					e.printStackTrace();
					System.exit(0);
				}
				actions = response;
				problem.applyActions(actions);

				++numUpdates;

				if (problem.inFailureState()||problem.inGoalState()){
					if(problem.getScore() > bestRun)
						bestRun = problem.getScore();
					//--- Failure occurred. ---
					if(problem.inFailureState()){
						problem.reset();
						failures++;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		RL_Learner.incUpdates(numUpdates);
		return bestRun;
	}
}
