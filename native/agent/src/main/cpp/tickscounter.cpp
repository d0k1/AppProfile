#include "tickscounter.h"
TicksCounter *timerCounter = new TicksCounter();

static void hpet_alarm(int val) {
    timerCounter->increaseCounter();
}

void TicksCounter::increaseCounter()
{
    counter++;
}

unsigned long long TicksCounter::getCounter()
{
    return counter;
}

TicksCounter::TicksCounter()
{
    struct hpet_info info;
    int r, value;

    sigemptyset(&new_.sa_mask);
    new_.sa_flags = 0;
    new_.sa_handler = hpet_alarm;

    sigaction(SIGIO, NULL, &old_);
    sigaction(SIGIO, &new_, NULL);

    fd = open("/dev/hpet", O_RDONLY);
    if (fd < 0) {
        fprintf(stderr, "ERROR: Failed to open /dev/hpet\n");
        fallback();
    }

    if ((fcntl(fd, F_SETOWN, getpid()) == 1) || ((value = fcntl(fd, F_GETFL)) == 1) || (fcntl(fd, F_SETFL, value | O_ASYNC) == 1))
    {
        fprintf(stderr, "ERROR: fcntl failed\n");
        //retval = 1;
        fallback();
    }
    if (ioctl(fd, HPET_IRQFREQ, freq) < 0)
    {
        fprintf(stderr, "ERROR: Could not set /dev/hpet to have a %2dHz timer\n", freq);
        //retval = 2;
        fallback();
    }

    if (ioctl(fd, HPET_INFO, &info) < 0)
    {
        fprintf(stderr, "ERROR: failed to get info\n");
        //retval = 3;
        fallback();
    }

    //fprintf(stdout, "\nhi_ireqfreq: 0x%lx hi_flags: %0x%lx hi_hpet: 0x%x hi_timer: 0x%x\n\n", info.hi_ireqfreq, info.hi_flags, info.hi_hpet, info.hi_timer);

    r = ioctl(fd, HPET_EPI, 0);
    if (info.hi_flags && (r < 0))
    {
        fprintf(stderr, "ERROR: HPET_EPI failed\n");
        //retval = 4;
        fallback();
    }

    if (ioctl(fd, HPET_IE_ON, 0) < 0)
    {
        fprintf(stderr, "ERROR: HPET_IE_ON failed\n");
        //retval = 5;
        fallback();
    }


}

void TicksCounter::fallback()
{
    sigaction(SIGIO, &old_, NULL);
}

TicksCounter::~TicksCounter()
{
    if (ioctl(fd, HPET_IE_OFF, 0) < 0)
    {
        //fprintf(stderr, "ERROR: HPET_IE_OFF failed\n");
        //retval = 6;
    }

    fallback();
}

