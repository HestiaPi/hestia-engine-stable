#!/bin/bash  

ip addr show wlan0 | grep inet | awk '{print $2}' | cut -d/ -f1;
