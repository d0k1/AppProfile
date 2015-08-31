#include "tickscounter.h"
#include "agentruntime.h"
#include <boost/format.hpp>

extern AgentRuntime *runtime;

static void hpet_alarm(int val) {
    runtime->incrementTicks();
}

void TicksCounter::increaseCounter()
{
    counter++;
}

unsigned long long TicksCounter::getCounter()
{
    return counter;
}

TicksCounter::TicksCounter(AgentOptions *option)
{
    int freq = option->getTimerFrequency();

    if(freq==0)
    {
        runtime->logInfo("HPET Timer disabled");
        return;
    }

    struct hpet_info info;
    int r, value;

    sigemptyset(&new_.sa_mask);
    new_.sa_flags = 0;
    new_.sa_handler = hpet_alarm;

    sigaction(SIGIO, NULL, &old_);
    sigaction(SIGIO, &new_, NULL);

    fd = open("/dev/hpet", O_RDONLY);
    if (fd < 0) {
        runtime->logError("ERROR: Failed to open /dev/hpet");
        fallback();
    }

    if ((fcntl(fd, F_SETOWN, getpid()) == 1) || ((value = fcntl(fd, F_GETFL)) == 1) || (fcntl(fd, F_SETFL, value | O_ASYNC) == 1))
    {
        runtime->logError("ERROR: fcntl failed");
        fallback();
    }
    if (ioctl(fd, HPET_IRQFREQ, freq) < 0)
    {
        runtime->logError((boost::format("ERROR: Could not set /dev/hpet to have a %2dHz timer") % freq).str());
        fallback();
    }

    if (ioctl(fd, HPET_INFO, &info) < 0)
    {
        runtime->logError("ERROR: failed to get info");
        fallback();
    }

    runtime->logInfo((boost::format("HPET Timer on %dHz frequency") % freq).str());
    runtime->logInfo((boost::format("hi_ireqfreq: 0x%lx hi_flags: 0x%lx hi_hpet: 0x%x hi_timer: 0x%x") % info.hi_ireqfreq % info.hi_flags % info.hi_hpet % info.hi_timer).str());

    r = ioctl(fd, HPET_EPI, 0);
    if (info.hi_flags && (r < 0))
    {
        runtime->logError("ERROR: HPET_EPI failed");
        fallback();
    }

    if (ioctl(fd, HPET_IE_ON, 0) < 0)
    {
        runtime->logError("ERROR: HPET_IE_ON failed");
        fallback();
    }
}

void TicksCounter::fallback()
{
    sigaction(SIGIO, &old_, NULL);
}

TicksCounter::~TicksCounter()
{
    if(fd<0){
        return;
    }
    if (ioctl(fd, HPET_IE_OFF, 0) < 0)
    {
        runtime->logError("ERROR: HPET_IE_OFF failed");
    }

    fallback();
}

