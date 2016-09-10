#!/bin/sh

rm -f /etc/init.d/mci-lr
rm -f /etc/default/mci-lr
rm -f /var/run/mci-lr

#Remove mci-lr from chkconfig
chkconfig --del mci-lr || true
