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
export CLASSPATH=./src:./properties:./bin:./src/anji_2_01/lib/anji.jar:./src/log4j.jar:./src/anji_2_01_actlog.jar

javac -classpath $CLASSPATH src/example/*.java
