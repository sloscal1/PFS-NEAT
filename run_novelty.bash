#! /bin/bash

#Set up the classpath
classpath=bin:properties
for lib in `ls lib/*.jar`; do
  classpath=${classpath}:$lib
done
classpath=$classpath:/home/sloscal1/Programming/jars/commons-math3-3.5/commons-math3-3.5.jar

prop=$1
prefix=${prop%%_*}
main=mil.af.rl.novelty.NoveltyEvolver

#Make sure the output directory exists (you can just make it yourself and cut this)
outputDir=(`grep "persistence.base.dir" properties/$prop | sed -e "s|persistence.base.dir=\(.*\)|\1|"`)
if [ ! -d "$outputDir" ]; then
  mkdir -p "$outputDir"
fi

#Now run the program
#Note that SAFS and PFS are RAM intensive due to stored samples, you
#may need to alter those parameters if you are resource constrained.
java -cp $classpath -Xmx2G $main $prop &> ${outputDir}/${prop}.txt

