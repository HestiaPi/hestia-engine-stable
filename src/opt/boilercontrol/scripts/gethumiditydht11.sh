#!/bin/bash  

sudo /opt/boilercontrol/scripts/Adafruit_DHT11_PIN4 | awk '{print $4}' 
