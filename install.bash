#! /bin/bash
LOCAL_JAR_DIR=/home/sloscal1/jars
DEP_DIR=/home/sloscal1/git/PFS-NEAT/deps
OLD_DIR=$(pwd)

if [ ! -d $DEP_DIR ]; then
  mkdir -p $DEP_DIR
fi

cd $DEP_DIR

#These are needed for the PFS stuff
printf "Get Apache Commons Math"
wget http://www.eu.apache.org/dist//commons/math/source/commons-math3-3.5-bin.tar.gz
tar xf commons-math3-3.5-bin.tar.gz
rm commons-math3-3.5-bin.tar.gz
cp commons-math3-3.5/*.jar $LOCAL_JAR_DIR/

printf "Get Apache Commons Lang"
wget http://www.us.apache.org/dist//commons/lang/binaries/commons-lang3-3.4-bin.tar.gz
tar xf commons-lang3-3.4-bin.tar.gz
rm commons-lang3-3.4-bin.tar.gz
cp commons-lang3-3.4/*.jar $LOCAL_JAR_DIR/

printf "Getting Weka"
wget -O weka-3.6.13.zip http://downloads.sourceforge.net/project/weka/weka-3-6/3.6.13/weka-3-6-13.zip?r=http%3A%2F%2Fwww.cs.waikato.ac.nz%2Fml%2Fweka%2Fdownloading.html&ts=1449184500&use_mirror=iweb
unzip weka-3.6.13.zip
rm weka-3.6.13.zip
cp weka-3-6-13/weka.jar $LOCAL_JAR_DIR/

#These are needed for the basic NEAT stuff
printf "Getting ANJI and deps"
wget -O anji-2.01.zip http://downloads.sourceforge.net/project/anji/anji/anji_2_01/anji_2_01.zip?r=http%3A%2F%2Fsourceforge.net%2Fprojects%2Fanji%2Ffiles%2F&ts=1449183953&use_mirror=skylineservers
unzip anji-2.01.zip
rm anji-2.01.zip
cp anji_2_01/libs/*.jar $LOCAL_JAR_DIR/


cd $OLD_DIR 
printf "You're going to want to do something like the following to compile the PFS-NEAT code:"
printf "export CLASSPATH=$LOCAL_JAR_DIR/weka.jar:$LOCAL_JAR_DIR/anji.jar:$LOCAL_JAR_DIR/jakarta-regexp-1.5jar:$LOCAL_JAR_DIR/jgap.jar:$LOCAL_JAR_DIR/log4j.jar:$LOCAL_JAR_DIR/commons-math3-3.5.jar:$LOCAL_JAR_DIR/commons-lang3-3.4.jar"
