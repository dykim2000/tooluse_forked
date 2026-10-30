#!/bin/bash

# LICENSE note: This file is slightly different from the original anji code
# - Yoonsuck Choe 

#echo off
export MYCLASSPATH=./properties
for i in `ls ./lib/*.jar`
do 
	export MYCLASSPATH=${MYCLASSPATH}:${i}
done
echo ${MYCLASSPATH}
java -classpath ${MYCLASSPATH} -Xms256m -Xmx384m com.anji.neat.Evolver $1 
