package com.focusit.agent.metrics.samples;

import java.nio.LongBuffer;

/**
 * Class to hold JVM metrics sample
 * <p/>
 * Created by Denis V. Kirpichenkov on 08.12.14.
 */
public final class JvmInfo implements Sample<JvmInfo> {

	public static int sizeOf() {
		// 8 byte per field * 24 fields
		return 8 * 24;
	}

	public long ipv4Addr1 = -1;
	public long ipv4Addr2 = -1;
	public long ipv4Addr3 = -1;
	public long ipv4Addr4 = -1;
	public long pid;
	public long loadedClassCount;
	public long heapCommited;
	public long heapInit;
	public long heapMax;
	public long heapUsed;
	public double processCpuLoad;
	public double systemCpuLoad;
	public long freePhysMem;
	public long freeSwap;
	public long totalPhysMem;
	public long totalSwap;
	public long threadCount;
	public long threadDaemonCount;
	public long peakThreadCount;
	public long totalStartedThreadCount;
	public long lastTimeGc1;
	public long lastTimeGc2;
	public long totalCountGc1;
	public long totalCountGc2;

	@Override
	public Sample<JvmInfo> copyDataFrom(Sample<JvmInfo> sample) {
		JvmInfo in = (JvmInfo) sample;

		ipv4Addr1 = in.ipv4Addr1;
		ipv4Addr2 = in.ipv4Addr2;
		ipv4Addr3 = in.ipv4Addr3;
		ipv4Addr4 = in.ipv4Addr4;
		pid = in.pid;
		loadedClassCount = in.loadedClassCount;
		heapCommited = in.heapCommited;
		heapInit = in.heapInit;
		heapMax = in.heapMax;
		heapUsed = in.heapUsed;
		freePhysMem = in.freePhysMem;
		freeSwap = in.freeSwap;
		totalPhysMem = in.totalPhysMem;
		totalSwap = in.totalSwap;
		threadCount = in.threadCount;
		threadDaemonCount = in.threadDaemonCount;
		peakThreadCount = in.peakThreadCount;
		totalStartedThreadCount = in.totalStartedThreadCount;
		lastTimeGc1 = in.lastTimeGc1;
		lastTimeGc2 = in.lastTimeGc2;
		totalCountGc1 = in.totalCountGc1;
		totalCountGc2 = in.totalCountGc2;

		processCpuLoad = in.processCpuLoad;
		systemCpuLoad = in.systemCpuLoad;

		return this;
	}

	@Override
	public void writeToLongBuffer(LongBuffer out) {
		out.put(ipv4Addr1);
		out.put(ipv4Addr2);
		out.put(ipv4Addr3);
		out.put(ipv4Addr4);
		out.put(pid);
		out.put(loadedClassCount);
		out.put(heapCommited);
		out.put(heapInit);
		out.put(heapMax);
		out.put(heapUsed);
		out.put(freePhysMem);
		out.put(freeSwap);
		out.put(totalPhysMem);
		out.put(totalSwap);
		out.put(threadCount);
		out.put(threadDaemonCount);
		out.put(peakThreadCount);
		out.put(totalStartedThreadCount);
		out.put(lastTimeGc1);
		out.put(lastTimeGc2);
		out.put(totalCountGc1);
		out.put(totalCountGc2);

		out.put(Double.doubleToLongBits(processCpuLoad));
		out.put(Double.doubleToLongBits(systemCpuLoad));
	}

	@Override
	public void readFromLongBuffer(LongBuffer in) {
		ipv4Addr1 = in.get();
		ipv4Addr2 = in.get();
		ipv4Addr3 = in.get();
		ipv4Addr4 = in.get();
		pid = in.get();
		loadedClassCount = in.get();
		heapCommited = in.get();
		heapInit = in.get();
		heapMax = in.get();
		heapUsed = in.get();
		freePhysMem = in.get();
		freeSwap = in.get();
		totalPhysMem = in.get();
		totalSwap = in.get();
		threadCount = in.get();
		threadDaemonCount = in.get();
		peakThreadCount = in.get();
		totalStartedThreadCount = in.get();
		lastTimeGc1 = in.get();
		lastTimeGc2 = in.get();
		totalCountGc1 = in.get();
		totalCountGc2 = in.get();

		processCpuLoad = Double.longBitsToDouble(in.get());
		systemCpuLoad = Double.longBitsToDouble(in.get());

	}

	@Override
	public int sizeOfSample() {
		return 0;
	}

	@Override
	public String toString() {
		return "JvmInfo{" +
			"ipv4Addr1=" + ipv4Addr1 +
			", ipv4Addr2=" + ipv4Addr2 +
			", ipv4Addr3=" + ipv4Addr3 +
			", ipv4Addr4=" + ipv4Addr4 +
			", pid=" + pid +
			", loadedClassCount=" + loadedClassCount +
			", heapCommited=" + heapCommited +
			", heapInit=" + heapInit +
			", heapMax=" + heapMax +
			", heapUsed=" + heapUsed +
			", processCpuLoad=" + processCpuLoad +
			", systemCpuLoad=" + systemCpuLoad +
			", freePhysMem=" + freePhysMem +
			", freeSwap=" + freeSwap +
			", totalPhysMem=" + totalPhysMem +
			", totalSwap=" + totalSwap +
			", threadCount=" + threadCount +
			", threadDaemonCount=" + threadDaemonCount +
			", peakThreadCount=" + peakThreadCount +
			", totalStartedThreadCount=" + totalStartedThreadCount +
			", lastTimeGc1=" + lastTimeGc1 +
			", lastTimeGc2=" + lastTimeGc2 +
			", totalCountGc1=" + totalCountGc1 +
			", totalCountGc2=" + totalCountGc2 +
			'}';
	}
}
