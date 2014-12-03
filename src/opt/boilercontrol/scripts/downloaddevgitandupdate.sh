#!/bin/sh

sudo service boilercontrol stop
cd /home/pi
sudo rm -rf /home/pi/hestia-engine-dev
git clone https://github.com/gulliverrr/hestia-engine-dev.git
cd /home/pi/hestia-engine-dev/src/opt/
sudo cp -rf * /opt
cd /
sudo rm -rf /home/pi/hestia-engine-dev
cd /opt/boilercontrol
sudo ./compile.sh
cd /
sudo rm -rf /hestia-web-dev/
git clone https://github.com/gulliverrr/hestia-web-dev.git
sudo cp -rf /hestia-web-dev/var/ .
sudo rm -rf /hestia-web-dev/
sudo service boilercontrol start
