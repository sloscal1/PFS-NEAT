/**
 * This package contains classes which act as an intermediary between the
 * the Progressive Feature Selection framework and the ANJI implementation of the
 * standard NEAT algorithm.
 * <br />
 * The <code>AddConnectionMutationOperator[Fixed|NoInput]</code> classes offer
 * alternatives to the standard add connection mutation operator found in ANJI.
 * The standard implementation involves a probabilistic algorithm which performs
 * pathologically poorly in later generations of FS-NEAT runs, and can be replaced
 * by the <code>AddConnectionMutationOperatorFixed</code> class in the properties
 * file. The NoInput variant prevents input connections from being added as a result of
 * genetic mutations, which is good to control when testing the performance of the
 * PFS framework.
 * <br />
 * FilePersistence alters the way in which the base paths are interpretted to allow for the
 * PERSISTENCE folder and xml files to be stored closer to the rest of the resulting information
 * about a run. This facilitates some of the analysis that we conduct on the runs.
 * <br />
 * NeatChromosomeUtility provides variants of some of the existing static methods
 * found in <code>com.anji.neat.NeatChromosomeUtility</code> and adds other utility
 * methods useful for manipulating the input nodes of NNs.
 * <br />
 * NeatConfigurationAdapter also exposes whether or not the network is configured to be fully
 * connected, which is used for FS-NEAT.
 */
package mil.af.rl.anji;