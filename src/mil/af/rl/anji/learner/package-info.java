/**
 * This package contains the primary algorithmic changes to the ANJI code base to
 * get the predictive feature selection framework implemented. The standard ANJI
 * Evolver class has been replaced by this version of Evolver which supports the
 * collection of sample observations during learning, as well as changing the 
 * feature subset of the current genotype. ConcurrentFitnessFunction enables the
 * genotype to be evaluated in parallel, and the NEAT_Learner classes are the
 * other halves of the evaluation process. NEAT_LearnerPFS allows for sample
 * collection during the evaluation process (which can save time when compared with
 * going back to re-evaluate chromosomes at the end of a NEAT generation).
*/
package mil.af.rl.anji.learner;