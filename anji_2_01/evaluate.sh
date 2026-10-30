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

my_cmd_line=""
until [ -z "$1" ]  # Until all parameters used up...
do
  my_cmd_line="$my_cmd_line $1"
  shift
done

java -classpath ${MYCLASSPATH} -Xms256m -Xmx384m com.anji.integration.Evaluator $my_cmd_line 
