#!/bin/bash  

if (( `id -u` != 0 )); then { MSG="Sorry, must be root.  Exiting..."; echo $MSG; exit; } fi

echo "Touching source files"
find . -name '*.java' | xargs touch
LIBS=libs/raspberrygpio.jar:/home/pi/java:libs/commonsio24.jar:libs/mysql-connector-java-5.1.21-bin.jar:libs/framboos-0.0.1.jar:libs/pi4j-core-0.0.2-SNAPSHOT.jar:./
echo "Compiling"
javac -classpath $LIBS uk/co/jaynne/BoilerControl.java
echo "Done, running"
java -cp $LIBS uk/co/jaynne/BoilerControl
