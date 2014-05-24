PFS-NEAT
========

COMING SOON (~20140525).

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
2014  Steven Loscalzo

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
* `src` Self-explanatory, check out the javadocs for more details.
* `lib` Also self-explanatory, these are all freely available on the web, but included here for ease-of-use.
* `properties` Contains template properties files for running all combinations of input 
* `tracks` Contains track descriptions for the RARS environment. The publications describing PFS-NEAT and SAFS-NEAT use fiorano, but clkwis is another popular choice in related work.
* `run.bash` Is a simple script to help demonstrate how to run these algorithms.
* `README.md` Is this file!

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
* `dpb.feature.type` controls what type of additional features will be used in the DPB environment. Note that in both environments, there is a hard-coded number of relevant sensors (6 for DPB, 10 for RARS), and if `subspace.features` is larger than that number, then the feature type value will determine how the rest of those features are set. Valid options are `mil.af.rl.problem.DoublePoleBalance$JAAMASSensors` which are those as described in the PFS-NEAT publication, `mil.af.rl.problem.LaggedSensors` which copy the values of the relevant sensors from 5 time steps previous (0 in the first 5 time steps), `mil.af.rl.problem.DoublePoleBalance$RandomFeature` which are clipped Gaussian random sensors with mean 0.5 and std 0.25, but only report values in [0,1], and `mil.af.rl.problem.DoublePoleBalance$Stored` which replay values from a previously run policy in the environment. This is faster than actually running the other agents in the environment during learning.
* `dpb.feature.type.storedpath` is the file where JAAMASSenors and Stored sensors will look for the previous values (set to DPB_transitions in this work).
* 





