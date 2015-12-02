/**
 * Classes needed for implementations of Novelty Search algorithms are contained
 * in this package. These are the key interfaces:
 * <ul>
 * 	<li>MultiObj</li>
 * 	<li>BehaviorVectorGenerator</li>
 * 	<li>NoveltyDistanceFunction</li>
 * 	<li>NoveltyArchive</li>
 * </ul>
 * <p>
 * MultiObj assigns a new "fitness" score to each Chromosome in a population based on some way of assessing novelty. The implementing class
 * NoveltyObj simply takes the mean distance to each Chromosome's kNN in behavior space as a measure of novelty.
 * <p>
 * BehaviorVectorGenerator transforms a given Chromosome into a vector in some Behavior Space. FitnessBasedBehaviorGenerator is a simple
 * implementing class that broadcasts the fitness of a Chromosome into some n dimensional space, for example. Traditional novelty techniques
 * would rely on a problem-specific generator.
 * <p>
 * NoveltyDistanceFunction is a distance measure in behavior space. EucDistance is a simple Euclidean distance measure, but the interface is
 * provided to allow experimentation with this measure.
 * <p>
 * NoveltyArchive stores past behavior vectors to help with the search in behavior space. There are different ways of doing this, but a simple
 * RandomSamplingNoveltyArchive is provided to store a random fraction of the population at each generation.
 * <p>
 * Running Novelty Search algorithms requires using the main class from NoveltyEvolver so that is uses the org.jgap.NoveltyGenotypeAdapter to
 * evolve the population, and setting required parameters in a properties file (see properties/NEAT_NOVELTY_DPB.properties for an example).
*/
package mil.af.rl.novelty;