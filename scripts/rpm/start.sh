#!/bin/sh
nohup java -jar /opt/mci-lr/lib/mci-lr.jar >  /dev/null 2>&1 &
echo $! > /var/run/mci-lr/mci-lr.pid