package mil.af.rl.novelty;

import java.util.List;
import java.util.Map;

import org.jgap.Chromosome;

public interface MultiObj {
	static final String NOVELTY_OBJ_KEY = "novelty_obj";
	
	/**
	 * Given the list of Chromosomes, return an objective function value to be used on
	 * evolution. This method may assume that the chromosomes have valid fitness values based on
	 * the problem's objective function.
	 * 
	 * @param chroms must not be null, represents a population
	 * @return a map with an integer fitness value associated with each Chromosome id in the input.
	 */
	Map<Long, Integer> measureObjective(List<Chromosome> chroms);
}
