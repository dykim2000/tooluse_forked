#!/bin/bash

# <?xml version="1.0" encoding="UTF-8"?>
# <classpath>
# 	<classpathentry kind="src" path="src"/>
# 	<classpathentry kind="src" path="properties"/>
# 	<classpathentry kind="lib" path="src/log4j.jar"/>
# 	<classpathentry kind="con" path="org.eclipse.jdt.launching.JRE_CONTAINER"/>
# 	<classpathentry kind="lib" path="src/anji_2_01_actlog.jar"/>
# 	<classpathentry kind="output" path="bin"/>
# </classpath>

#export CLASSPATH=src/anji_2_01.jar:src/log4j.jar:src/anji_2_01_actlog.jar
export CLASSPATH=./bin:./src:./properties:./src/anji_2_01/lib/anji.jar:./src/log4j.jar:./src/anji_2_01_actlog.jar:./src/example

# run example.EvaluateIt to evaluate a single chromosome
# usage: ./run.sh [chromosome_id] [properties_file]
java -classpath $CLASSPATH example.EvaluateIt "$@"
