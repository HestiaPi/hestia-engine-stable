#!/bin/sh
DATE=$(date +%Y%m%d)
MAC=$(ip addr show eth0 | grep link | awk '{print $2}')
find /var/log/ -name '*.log' | xargs tar --no-recursion -czf alllogs.tgz
mv alllogs.tgz ${MAC}_${DATE}.tgz
