#!/bin/bash
#echo off
export MYCLASSPATH=./properties
for i in `ls ./lib/*.jar`
do 
	export MYCLASSPATH=${MYCLASSPATH}:${i}
done
echo ${MYCLASSPATH}

java -classpath ${MYCLASSPATH} com.anji.navi.NaviEvaluator $1  $2
