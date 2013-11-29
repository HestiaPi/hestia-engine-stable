#!/bin/sh
DATE=$(date +%Y%m%d)
MAC=$(ip addr show eth0 | grep link | awk '{print $2}')
NAME=${MAC}_${DATE}
find /var/log/ -mtime -1 | xargs tar --no-recursion -czf alllogs.tgz
mv alllogs.tgz $NAME.tgz
ftp -i -n isharemyprojects.com <<EOF
user hestialogger@webpuzz.com password
cd /
lcd .
mput $NAME.tgz
quit
EOF
rm $NAME.tgz
