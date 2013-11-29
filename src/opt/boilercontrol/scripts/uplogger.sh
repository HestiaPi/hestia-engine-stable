#!/bin/sh
DATE=$(date +%Y%m%d)
find /var/log/ -name '*.log' | xargs tar --no-recursion -czf alllogs.tgz
mv alllogs.tgz $DATE.tgz
ftp -i -n isharemyprojects.com <<EOF
user hestialogger@webpuzz.com password
cd /1/
lcd .
mput $DATE.tgz
quit
EOF
rm $DATE.tgz
