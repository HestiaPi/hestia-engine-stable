#!/bin/bash  

################################
#sudo modprobe w1-gpio         #
#sudo modprobe w1-therm        #
################################

FILE=$(ls /sys/bus/w1/devices/ | sort -f | head -1);
cat /sys/bus/w1/devices/$FILE/w1_slave | grep t= | cut -d= -f2;

