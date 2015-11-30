package mil.af.rl.novelty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgap.Chromosome;

import com.anji.util.Configurable;
import com.anji.util.Properties;

import mil.af.rl.anji.learner.RL_Learner;
import mil.af.rl.problem.RLProblem;

public class NoveltyObj implements MultiObj, Configurable {
	private Properties config;
	private BehaviorVectorGenerator bvg = new RandomSampleBehaviorGenerator();
	private NoveltyArchive archive;
	private NoveltyDistanceFunction dist = new EucDistance();
	private int k;
	
	@Override
	public void init(Properties arg0) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Map<Long, Integer> measureObjective(List<Chromosome> chroms) {
		RLProblem prob = (RLProblem)config.singletonObjectProperty(RL_Learner.PROBLEM_KEY);
		//Put each chromosome into the behavior space
		for(Chromosome ch : chroms){
			prob.reset();
			archive.put(bvg.generateBehaviorVector(ch), ch.getId());
		}
		
		//Find the k-NN of each to set the novelty value:
		Map<Long, Integer> novelty = new HashMap<>();
		for(Chromosome ch : chroms){
			Long id = ch.getId();
			double[] indiv = archive.get(id);
			List<double[]> nn = archive.getKNN(k, id);
			double nov = 0.0;
			for(double[] neighbor : nn)
				nov += dist.distance(indiv, neighbor);
			novelty.put(id, (int)Math.round((nov / nn.size()) * 1000));
		}
		
		//Update the novelty archive according to its current policy
		archive.update(chroms.size());
		
		return novelty;
	}
}
