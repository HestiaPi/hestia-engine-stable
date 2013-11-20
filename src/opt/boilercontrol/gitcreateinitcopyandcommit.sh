#!/bin/bash

# Run like that:
# sudo ./gitcreateinitcopyandcommit.sh "<YOUR-COMMIT-COMMENT-HERE>"

rm -rf hestia-dev;
mkdir hestia-dev;
cd hestia-dev;
git init;
git remote add origin https://github.com/gulliverrr/hestia-dev.git;
git pull origin master;
sudo cp -r /opt/boilercontrol/* src/opt/boilercontrol/;
sudo find . -name "*.class" -exec rm -rf {} \;
sudo git add src;
sudo git commit -m "$1";
git push origin master;

