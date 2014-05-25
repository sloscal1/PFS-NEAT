PFS-NEAT
========
#Overview#
Implementations of several feature selection algorithms that use the NEAT genetic search method.
The Predictive Feature Selection embedded in NEAT project gives implementations of several algorithms for scaling up direct genetic direct policy search methods to environments with many (possibly irrelevant or redundant) state variables. The code on this site contains an implementations of this work, and several other competing feature selection algorithms that use the NEAT genetic search algorithm. The papers these algorithms are based on can be found here:
* PFS-NEAT from Loscalzo, S., Wright, R., and Yu, Lei. (2014) Predictive Feature Selection for Genetic Policy Search. (To appear)
* SAFS-NEAT from Loscalzo, S., Wright, R., Acunto, K., and Yu, Lei. (2012) Sample Aware Feature Selection Embedded for Reinforcement Learning. In Proceedings of GECCO, pp 879-886.
* FD-NEAT from Tan, M., Hartley, M., Bister, M., and Deklerck R. (2009)   Automated Feature Selection in Neuroevolution. In Evolutionary Intelligence 1(4):271-292.
* FS-NEAT from Whiteson S., Stone, P., and Stanley, K.O. (2005) Automatic Feature Selection in Neuroevolution. In Proceedings of GECCO, pp 1225-1232.
* NEAT from Stanley, K.O., and Miikkulainen R. (2002) Efficient Reinforcement Learning through Evolving Neural Network Topologies. In Proceedings of GECCO, pp 569 - 577.

#Licensing#
PFS-NEAT is an implementation of the Predictive Feature Selection Framework, with an implementation embedded in NEAT.

2014  Steven Loscalzo (sloscalzo85@gmail.com)

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, US.A 

#Dependencies#
This code uses the [ANJI](http://anji.sourceforge.net/) implementation of NEAT which gives us NEAT, FS-NEAT, and FD-NEAT via different parameter settings (explained later). ANJI introduces dependencies on [Apache log4j](http://logging.apache.org/log4j/2.x/), [Apache Jakarta Regexp 1.5](http://archive.apache.org/dist/jakarta/regexp/) and [jgap](http://jgap.sourceforge.net/), and we have included the versions of those libraries we used in the lib directory. We add dependencies on [weka 3.6.11](http://www.cs.waikato.ac.nz/ml/weka/), [Apache Commons Lang 3.3.1](http://commons.apache.org/proper/commons-lang/), and [Apache Commons Math 3.3.0](http://commons.apache.org/proper/commons-math/) to implement PFS-NEAT and SAFS-NEAT.


#Usage#
Download the project including all support files.
To run an experiment using one of the algorithms, enter the project directory and execute the command:
`./run.bash <1>_NEAT_<2>.properties`
Where <1> is one of {FS, FD, SAFS, PFS}, and <2> is RARS or DPB. To run NEAT, omit <1> and the first _. This script is dependent on bash, but it is straight-forward enough to run on other systems with Java and the aforementioned libraries installed. The argument given to the run.bash script is the name of a properties file contained within the properties directory. Generally, each run of the algorithm requires its own properties file that varies the random seed. Each properties file lists the parameters of NEAT and the specific feature selection variant along with the type and parameters of the experimental domain. We have provided two experimental domains in this release (Double Inverted Pendulum Balance (DPB), and the Robot Auto Racing Simulator (RARS)). We have released 10 properties files, 1 for each combination of algorithm and domain.

When the script terminates, you will find a directory called Output, and there will be a .txt file inside a few more directories that corresponds to the output of the run.  Since these algorithms are stochastic, general performance values should be obtained by running these algorithms many times and averaging the results.

#Contents#
* `src` self-explanatory, check out the javadocs for more details.
* `lib` also self-explanatory, these are all freely available on the web, but included here for ease-of-use.
* `properties` contains template properties files for running all combinations of input 
* `tracks` contains track descriptions for the RARS environment. The publications describing PFS-NEAT and SAFS-NEAT use fiorano, but clkwis is another popular choice in related work.
* `DPB_transitions` contains trajectory data for the JAAMASSensors and Stored irrelevant sensor types for the DPB environment(see below).
* `RARS_transitions` same as `DPB_transitions` except for the RARS environment.
* `run.bash` is a simple script to help demonstrate how to run these algorithms.
* `README.md` is this file!

#Properties File Descriptions#
Each properties file contains a list of key value pairs written like `key=value`, one per line. Most of the properties hail from ANJI, and you should consult that documentation for more detailed descriptions on how to set those values. Here we will describe the new properties that we have added or must be altered for our methods:
##Parameters to set across all algorithms##
* `learner.class` is the class that interacts with the problem domain. Set to `mil.af.rl.anji.learner.NEAT_LearnerPFS` for PFS-NEAT and SAFS-NEAT so that samples will be collected. Otherwise, set it to `mil.af.rl.anji.learner.NEAT_Learner`.
* `learner.problem.class` is the environment that will be run. Set it to either `mil.af.rl.problem.DoublePoleBalance` or `mil.af.rl.problem.rars.SimpleRars` for RARS.
* `learner.reversion_window` controls how long to wait before reverting back to the previous feature subset in PFS and SAFS. Set it to something larger than the number of learning iterations to prevent reversion from happening.
* `learner.iterations` controls the number of iterations to learn over. This should be the same as `num.generations`.
* `subspace.features` is the total number of state variables in the environment (include both relevant and irrelevant ones in this count).
* `subspace.actions` is the total number of action variables in the environment (1 for DPB, 2 for RARS).
* `fitness.numThreads` is the number of threads to use when evaluating the NEAT population. This value is only used when `fitness_function.class` is set to `mil.af.rl.anji.learner.ConcurrentFitnessFunction`.
* `fitness.activator_transcriber.class` should be set to `com.anji.integration.ActivatorTranscriber` for all algorithms.
* `persistence.class` should be set to `mil.af.rl.anji.FilePersistence` for all algorithms.

##Environment specific parameters##
Each environment has a few specific parameters, and only need to be included if that environment is the focus of a given properties file. Key prefixes denote which environment each parameter is used in.
* `dpb.feature.type` controls what type of additional features will be used in the DPB environment. Note that in both environments, there is a hard-coded number of relevant sensors (6 for DPB, 10 for RARS), and if `subspace.features` is larger than that number, then the feature type value will determine how the rest of those features are set. Valid options are `mil.af.rl.problem.DoublePoleBalance$JAAMASSensors` which are those as described in the PFS-NEAT publication, `mil.af.rl.problem.DoublePoleBalance$LaggedSensors` which copy the values of the relevant sensors from 5 time steps previous (0 in the first 5 time steps), `mil.af.rl.problem.DoublePoleBalance$RandomFeature` which are clipped Gaussian random sensors with mean 0.5 and std 0.25, but only report values in [0,1], and `mil.af.rl.problem.DoublePoleBalance$Stored` which replay values from a previously run policy in the environment. This is faster than actually running the other agents in the environment during learning.
* `dpb.feature.type.storedpath` is the file where JAAMASSenors and Stored sensors will look for the previous values (set to DPB_transitions in this work).
* `rars.args.track` is the name of the track file (from the `tracks` directory) to use.
* `rars.args.trackPath` is the path to the tracks directory.
* `rars.features.relevant` is the number of rangefinder sensors to use. Set to 9 in all PFS and SAFS publications.
* `rars.features.redundant` is not used in this work and should be removed in a future release.
* `rars.features.irrelevant` is the number of additional features to use.
* `rars.feature.type` is the kind of additional feature to use. The types are the same as for DPB, but are based from `mil.af.rl.problem.rars.SimpleRars$`.
* `rars.feature.type.storedpath` is used the same way as in DPB, and should point to the `RARS_transitions` file.

##FD-NEAT specific parameters##
* `remove.connection.mutation.rate` should be in the range (0,1).
* `initial.topology.fully.connected` should be set to true.
* `add.connection.class` can be set to either `com.anji.neat.AddConnectionMutationOperatorNoInput` or `...AddConnectionMutationOperatorFixed` (results will vary, the former was used in the PFS-NEAT publication).

##FS-NEAT specific parameters##
* `add.connection.mutation.rate` should be in the range (0,1).
* `initial.topology.fully.connected` should be set to false.
* `add.connection.class` must be set to `mil.af.rl.anji.AddConnectionMutationOperatorFixed`.

##PFS-NEAT/SAFS-NEAT shared parameters##
* `container.class` is the sample storage container. If RAM is an issue, use `mil.af.rl.predictive.FailureSeparatingSampleContainer`, otherwise some time can be saved if `mil.af.rl.predictive.EagerChromosomeSampleContainer`.
* `container.max_samples` is the maximum number of samples to store. Since current implementations store all samples in RAM, reasonable values should be selected based on your system.
* `container.eager.base.class` is the temporary storage container used while policies are being evaluated. This can be the FailureSeparating container mentioned above.
* `container.eager.k` is the number of policies to keep samples from. The top 5 most fit policies will be used.
* `selector.class` can be set to set up specific sampling schemes from the container. In the current implementation, the container itself does some sampling when necessary to keep the number of samples below the maximum, therefore we don't do any more sampling and use `mil.af.rl.predictive.IdentitySampleSelector`.
* `subspace.min_samples` sets a threshold for the number of samples to have in the container before a feature selection step can occur (regardless of how many generations is may take to collect all of these samples).
* `stagnation.class` is the first stagnation class. The components of stagnation have a large impact on the algorithms, and eventually a decorator pattern was used to allow composable stagnation classes to allow rapid experimentation. Unfortunately, this doesn't work well with properties files since keys must be unique. What we end up with is the .# syntax, where the parameters of the first class end with .1, parameters of the second end in .2, and so on. I will detail the stagnation settings used in the properties files as an example. This class is set to `mil.af.rl.predictive.SlidingWindowStagnation`.
* `stagnation.sliding.window.1` the first stagnation class's (SlidingWindow's) minimum time before performing another round of feature selection. Set to 10.
* `stagnation.sliding.factor.1` the scaling factor on the window. Set here to grow the window slightly over time (1.2).
* `stagnation.class.2` the second decorator used is `mil.af.rl.predictive.TrendStagnation`, which considers a populations stagnant if the fitness of the champion does not increase much.
* `stagnation.trend.window.2` sets the number of generations to take an average over to determine trend stagnation (10).
* `stagnation.trend.threshold.2` fitness change threshold (below this number is considered stagnant. Varies depending on max fitness of a particular problem.
* `progressive_learner.class` the learner must have support for changing of feature subsets, so we use a modified version of the standard Evolver `mil.af.rl.anji.learner.Evolver`.

##SAFS-NEAT specific parameters##
* `subspace_identification.class` is the feature selection algorithm to use, for SAFS it is `mil.af.rl.safs.DiscMISubspaceIdentifier` which uses mutual inforamtion between elements of samples from the sample container to determine feature relevance. It also discretizes the sample values before assessing relevance.
* `subspace_identification.dmi.sampl_relationship` defines which components of a sample to use in the above relevance measure. DELTA_STATE_ACTION is typical for SAFS (though others can be found in mil.af.rl.predictive.SampleUtilities). This one tells the above feature selection algorithm to compare changes in each state variable from one timestep to the next against the action variables.
* `subspace.discretize.predictors.bins` number of labels to use in the discretization of the state variable part of the above relevance measure.
* `subspace.discretize.response.bins` number of labels to use in the discretization of the actions part of the above relevance measure.
* `subspace_identification.dmi.strategy` controls how to select the features into the active subset after they have been ranked. In this case `mil.af.rl.safs.BestFeatureCriteria` was used, so that only the top scoring feature is added.

##PFS-NEAT specific parameters##
* `subspace_identification.class` PFS-NEAT uses `mil.af.rl.predictive.WekaIncSubspaceIdentifier` which means that it incrementally grows the subspace using Weka defined feature search and evaluation classes. Since feature relevance is divided into reward relevance and transition relevance components, and as such there are parameters controlling each phase separately.
* `weka.reward.search.class` the subset search class used to find the reward relevant set. We use a wrapper around the BestFirst search strategy `mil.af.rl.predictive.BestFirst` to allow settng of constructor parameters by properties file.
* `weka.attributeSelection.BestFirst.direction` is a constructor parameter to Weka's BestFirst implementation, and we use `FORWARD`.
* `weka.attributeSelection.BestFirst.term` controls how many non-improving layers of subsets to considered before terminating the search. We use 1.
* `weka.reward.eval.class` sets the evaluation scheme to use to guide the reward relevant subset. We use `weka.attributeSelection.CfsSubsetEval`.
* `weka.trans.search.class` gives the subset search technique for transition relevant sensors. We use `mil.af.rl.predictive.BestFirst`. Other parameters are shared with the reward search class.
* `weka.trans.eval.class` controls the evaluation measure to use when searching for transition relevant sensors. We use the same as reward eval, `weka.attributeSelection.CfsSubsetEval`, but it can be a different algorithm.
* `weka.single.addition` forces the feature selection step to change the subset size by at most 1 feature at a time if this is set to `true`. It is false by default.

I expect to make updates to this codebase as new features (hopefully) or bugs (hopefully not!) necessitate. 
Enjoy!
-Steve
