package mil.af.rl.anji.learner;

import java.util.ArrayList;
import java.util.List;

import mil.af.rl.predictive.ArraySample;
import mil.af.rl.predictive.EagerChromosomeSampleContainer;
import mil.af.rl.predictive.PFSInfo;
import mil.af.rl.predictive.Sample;
import mil.af.rl.predictive.SampleContainer;
import mil.af.rl.predictive.EagerChromosomeSampleContainer.ContainerInfo;
import mil.af.rl.predictive.Sample.SampleType;

import org.jgap.Chromosome;

import com.anji.integration.Activator;
import com.anji.integration.ActivatorTranscriber;
import com.anji.util.Configurable;

/**
 * This class evaluates a policy represented by a NEAT chromosome. This class should
 * be the learner when using the ProgressiveFeatureSelectionFramework code with PFS-NEAT or SAFS-NEAT.
 * 
 * @author Robert Wright
 * @author Steven Loscalzo modified to accept PFSInfo due to interface change and to record samples.
 */
public class NEAT_LearnerPFS extends RL_Learner implements Configurable
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

		//Eager collection requires a bit of handling by the chromosome.
		SampleContainer old = info.getContainer(chrom);
		long eagerChromContId = -1L;
		List<Sample> newSamples = new ArrayList<>();
		if(info.isCollecting(chrom) && old instanceof EagerChromosomeSampleContainer){
			ContainerInfo cinfo = ((EagerChromosomeSampleContainer) old).getFreeContainer();
			eagerChromContId = cinfo.getId();
			info.setSampleContainer(chrom, cinfo.getSampleContainer());
		}

		//BUILD the NN 
		Activator activator = null;
		double numSamples = 0.0;
		try {		
			activator = factory.newActivator( chrom );
			//--- Iterate through the action-learn loop. ---
			while (!problem.inGoalState() && failures < MAX_FAILURES){			

				//--- Execute the action. ---
				double[] netInput = problem.getNetInput();
				response = activator.next(netInput);
			
				actions = response;
				problem.applyActions(actions);
				//--- observe new State ---
				double reward = problem.getReward();
				double[] nextState = problem.getNetInput();

				++numUpdates;
				++numSamples;

				if (problem.inFailureState()||problem.inGoalState()){
					if(problem.getScore() > bestRun)
						bestRun = problem.getScore();
					//--- Failure occurred. ---
					if(problem.inFailureState()){
						problem.reset();
						failures++;
					}
				}
				if(info.isCollecting(chrom))
					newSamples.add(new ArraySample(netInput, actions, reward, nextState, SampleType.UNKNOWN, chrom.getId().intValue()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		//This was used to pass messages around the PFS code, it can probably be removed when using
		//most of the sample containers (need to verify).
		props.put("progessive.samples", numSamples);
		//Signal that it is time to evaluate these samples for more permanent storage
		if(info.isCollecting(chrom)){
			info.getContainer(chrom).addSamples(newSamples);
			if(old instanceof EagerChromosomeSampleContainer){
				((EagerChromosomeSampleContainer)old).evaluate(chrom.getId(), eagerChromContId, bestRun);
				info.setSampleContainer(chrom, old);
			}
		}
		RL_Learner.incUpdates(numUpdates);
		return bestRun;
	}
}
