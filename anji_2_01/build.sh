#!/bin/bash
#echo off
export MYCLASSPATH=./properties
for i in `ls ./lib/*.jar`
do 
	export MYCLASSPATH=${MYCLASSPATH}:${i}
done
echo ${MYCLASSPATH}
javac -classpath ${MYCLASSPATH} $1 

#cat <<EOF
#Done: do

	pushd src
	jar cvf ../lib/choe.jar com/anji/navi/NaviFitnessFunction.class \
	 com/anji/navi/NaviEvaluator.class  com/anji/navi/NaviDisplay.class
	popd

#EOF
