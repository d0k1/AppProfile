package com.focusit.utils.metrics;

import com.focusit.utils.metrics.samples.JvmInfo;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;

/**
 * Class implementing jvm monitoring according rules
 * <p/>
 * Created by Denis V. Kirpichenkov on 10.12.14.
 */
public class JvmMonitoring {
	private static final JvmMonitoring instance = new JvmMonitoring();

	private final OperatingSystemMXBean osMBean;
	private final ThreadMXBean threadMBean;
	private final MemoryMXBean memoryMBean;

	// Max samples in memory = 655360 * 176 (bytes per sample) = 125 829 120 bytes = 120 Mb
	private final static int samples = 655360;
	private final JvmInfo infos[] = new JvmInfo[samples];

	private JvmMonitoring() {
		osMBean = ManagementFactory.getOperatingSystemMXBean();
		threadMBean = ManagementFactory.getThreadMXBean();
		memoryMBean = ManagementFactory.getMemoryMXBean();
	}

	public JvmMonitoring getInstance() {
		return instance;
	}
}
