#! /bin/bash

#Set up the classpath
classpath=bin:properties
for lib in `ls lib/*.jar`; do
  classpath=${classpath}:$lib
done

#Determine the main class to run based on the properties file
#Note that all the algorithms could use PredictiveFeatureSelectionFramework as
#long as all the properties were set appropriately. Since FD, FS, and NEAT did
#not need anything extra, I just used the Evolver that they were originally
#designed for.
prop=$1
prefix=${prop%%_*}

#NEAT or FS-NEAT or FD-NEAT
main=com.anji.neat.Evolver
#NOVELTY SEARCH *-NEAT
#main=mil.af.rl.novelty.NoveltyEvolver

#PFS-NEAT or SAFS-NEAT
if [[ "$prefix" == "SAFS" || "$prefix" == "PFS" ]]; then
  main=mil.af.rl.predictive.PredictiveFeatureSelectionFramework
fi
#If you want to use novelty search with PFS/SAFS, use the Predictive... and set the learner class to the NoveltyEvolver in the properties file.

#Make sure the output directory exists (you can just make it yourself and cut this)
outputDir=(`grep "persistence.base.dir" properties/$prop | sed -e "s|persistence.base.dir=\(.*\)|\1|"`)
if [ ! -d "$outputDir" ]; then
  mkdir -p "$outputDir"
fi

#Now run the program
#Note that SAFS and PFS are RAM intensive due to stored samples, you
#may need to alter those parameters if you are resource constrained.
java -cp $classpath -Xmx4G $main $prop &> ${outputDir}/${prop}.txt

