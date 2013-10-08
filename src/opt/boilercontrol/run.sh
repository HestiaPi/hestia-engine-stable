#!/bin/bash  

if (( `id -u` != 0 )); then { MSG="Sorry, must be root.  Exiting..."; echo $MSG; exit; } fi

LIBS=libs/raspberrygpio.jar:/home/pi/java:libs/commonsio24.jar:libs/mysql-connector-java-5.1.21-bin.jar:libs/framboos-0.0.1.jar:libs/pi4j-core.jar:./
sudo java -cp $LIBS uk/co/jaynne/BoilerControl