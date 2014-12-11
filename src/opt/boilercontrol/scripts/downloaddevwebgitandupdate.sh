#!/bin/bash

if (( `id -u` != 0 )); then { MSG="Sorry, must be root.  Exiting..."; echo $MSG; exit; } fi

cd /
sudo rm -rf /hestia-web-dev/
git clone https://github.com/gulliverrr/hestia-web-dev.git
sudo cp -rf /hestia-web-dev/var/ .
sudo rm -rf /hestia-web-dev/
