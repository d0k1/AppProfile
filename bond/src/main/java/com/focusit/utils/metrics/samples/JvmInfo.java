package com.focusit.utils.metrics.samples;

import java.nio.LongBuffer;

/**
 * Class to hold JVM metrics sample
 * <p/>
 * Created by Denis V. Kirpichenkov on 08.12.14.
 */
public class JvmInfo implements Sample<JvmInfo> {
//	static {
//		String name = ManagementFactory.getRuntimeMXBean().getName();
//		int p = name.indexOf('@');
//		String pid = name.substring(0, p);
//
//		pid = ManagementFactory.getRuntimeMXBean().getName();
//	}

	public int sizeOf() {
		// 8 byte per field * 24 fields
		return 8 * 24;
	}

	public long ipv4Addr1;
	public long ipv4Addr2;
	public long ipv4Addr3;
	public long ipv4Addr4;
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
		return null;
	}

	@Override
	public void writeToLongBuffer(LongBuffer buffer) {

	}

	@Override
	public void readFromLongBuffer(LongBuffer buffer) {

	}

	@Override
	public int sizeOfSample() {
		return 0;
	}
}
