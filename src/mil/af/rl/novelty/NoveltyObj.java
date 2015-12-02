package mil.af.rl.novelty;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgap.Chromosome;

import com.anji.util.Configurable;
import com.anji.util.Properties;

import mil.af.rl.problem.RLProblem;

public class NoveltyObj implements MultiObj, Configurable {
	public static final String BEHAVIOR_GEN_KEY = "behavior_generator";
	public static final String NOVELTY_ARCHIVE_KEY = "archive";
	public static final String NOVELTY_K_KEY = "knn";
	
	private BehaviorVectorGenerator bvg = new RandomSampleBehaviorGenerator();
	private NoveltyArchive archive;
	private NoveltyDistanceFunction dist = new EucDistance();
	private int k;
	private RLProblem problem;
	
	@Override
	public void init(Properties props) throws Exception {
		bvg = (BehaviorVectorGenerator)props.newObjectProperty(BEHAVIOR_GEN_KEY);
		archive = (NoveltyArchive)props.newObjectProperty(NOVELTY_ARCHIVE_KEY);
		k = props.getIntProperty(NOVELTY_K_KEY);
		problem = (RLProblem)props.singletonObjectProperty("learner.problem");
	}
	
	@Override
	public Map<Long, Integer> measureObjective(List<Chromosome> chroms) {
		//Put each chromosome into the behavior space
		for(Chromosome ch : chroms){
			problem.reset();
			archive.put(bvg.generateBehaviorVector(ch), ch.getId());
		}
		
		//Find the k-NN of each to set the novelty value:
		Map<Long, Integer> novelty = new HashMap<>();
		for(Chromosome ch : chroms){
			Long id = ch.getId();
			double[] indiv = archive.get(id);
			List<double[]> nn = archive.getKNN(k, indiv);
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
