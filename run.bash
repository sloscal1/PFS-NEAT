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
main=com.anji.neat.Evolver
if [[ "$prefix" == "SAFS" || "$prefix" == "PFS" ]]; then
  main=mil.af.rl.predictive.PredictiveFeatureSelectionFramework
fi

#Make sure the output directory exists (you can just make it yourself and cut this)
outputDir=(`grep "persistence.base.dir" properties/$prop | sed -e "s|persistence.base.dir=\(.*\)|\1|"`)
if [ ! -d "$outputDir" ]; then
  mkdir -p "$outputDir"
fi

#Now run the program
#Note that SAFS and PFS are RAM intensive due to stored samples, you
#may need to alter those parameters if you are resource constrained.
java -cp $classpath -Xmx4G $main $prop &> ${outputDir}/${prop}.txt

