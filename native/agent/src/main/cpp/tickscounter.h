#ifndef TICSCOUNTER_H
#define TICSCOUNTER_H

#include <linux/hpet.h>
#include <sys/ioctl.h>
#include <sys/wait.h>
#include <signal.h>
#include <time.h>
#include <atomic>
#include <stdlib.h>
#include <fcntl.h>
#include <stdint.h>
#include <sys/ioctl.h>
#include <sys/types.h>
#include <unistd.h>
#include <stdio.h>

#include "agentoptions.h"

class AgentOptions;

using namespace std;

/**
    # set timer freq limit to 10 microseconds
    $ sudo echo 100000 > /proc/sys/dev/hpet/max-user-freq

    need permissions to /dev/hpet
    sudo chmod 666 /dev/hpet

    A simple chgrp might not be persistent across reboots - to make the change last, create a new 40-timer-permissions.rules file in /etc/udev/rules.d with the following lines:

    KERNEL=="rtc0", GROUP="audio"
    KERNEL=="hpet", GROUP="audio"

    /etc/sysctl.conf (or something like /etc/sysctl.d/60-max-user-freq.conf):

    dev.hpet.max-user-freq=100000

    Start-up script or /etc/rc.local:

    echo 100000 > /proc/sys/dev/hpet/max-user-freq
*/
class TicksCounter
{
    public:
        TicksCounter(AgentOptions *option);
        virtual ~TicksCounter();
        void increaseCounter();
        unsigned long long getCounter();
    protected:
    private:
        int fd=-1;
        struct sigaction old_, new_;
        atomic<unsigned long long> counter = {0};

        void fallback();
};

#endif // TICSCOUNTER_H
