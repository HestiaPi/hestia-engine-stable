#!/bin/bash  

iwconfig wlan0 | grep SSID | awk '{print $4}' | cut -d\" -f2;