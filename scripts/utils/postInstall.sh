#!/bin/sh

ln -s /opt/mci-lr/bin/mci-lr /etc/init.d/mci-lr
ln -s /opt/mci-lr/etc/mci-lr /etc/default/mci-lr
ln -s /opt/mci-lr/var /var/run/mci-lr

if [ ! -e /var/log/mci-lr ]; then
    mkdir /var/log/mci-lr
fi

# Add mci-lr service to chkconfig
chkconfig --add mci-lr