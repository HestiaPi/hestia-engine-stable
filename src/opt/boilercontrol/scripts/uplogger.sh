#!/bin/sh

DATE=$(date +%Y%m%d)
MAC=$(ip addr show eth0 | grep link | awk '{print $2}')
NAME=${MAC}_${DATE}
cat /opt/boilercontrol/version > /var/log/stats
cat /var/www/version >> /var/log/stats
uptime >> /var/log/stats
df -h / | grep dev >> /var/log/stats
sed "s/\(...\)$/.\1°C/" < /sys/class/thermal/thermal_zone0/temp >> /var/log/sta$
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
