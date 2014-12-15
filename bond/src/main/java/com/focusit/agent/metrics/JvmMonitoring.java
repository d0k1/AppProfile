package com.focusit.agent.metrics;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.samples.JvmInfo;
import com.focusit.agent.metrics.samples.Sample;
import com.focusit.agent.utils.common.FixedSamplesArray;
import com.sun.management.GarbageCollectorMXBean;
import com.sun.management.OperatingSystemMXBean;
import com.sun.management.ThreadMXBean;

import java.lang.management.ClassLoadingMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;

/**
 * Class implementing jvm monitoring according rules
 * <p/>
 * Created by Denis V. Kirpichenkov on 10.12.14.
 */
public class JvmMonitoring {

	private static final JvmMonitoring instance = new JvmMonitoring();

	private final OperatingSystemMXBean osMBean;
	private final ThreadMXBean threadMBean;
	private final ClassLoadingMXBean classLoadingMXBean;
	private final MemoryMXBean memoryMBean;
	private GarbageCollectorMXBean gc1;
	private GarbageCollectorMXBean gc2;

	// Max samples in memory = 655360 * 176 (bytes per sample) = 125 829 120 bytes = 120 Mb
	private final static int samples = 655360;
	private final static FixedSamplesArray<JvmInfo> data = new FixedSamplesArray<>(samples, new FixedSamplesArray.ItemInitializer() {
		@Override
		public Sample[] initData(int limit) {
			return new JvmInfo[limit];
		}

		@Override
		public Sample createItem() {
			return new JvmInfo();
		}
	}, "JvmStat");

	private final Thread monitoringThread;
	private final long pid;
	private final long ips[] = new long[4];

	private JvmMonitoring() {
		osMBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		threadMBean = (ThreadMXBean) ManagementFactory.getThreadMXBean();

		List gcMBeans = ManagementFactory.getGarbageCollectorMXBeans();
		memoryMBean = ManagementFactory.getMemoryMXBean();
		classLoadingMXBean = ManagementFactory.getClassLoadingMXBean();

		//Suppose there will no interface modification during jvm run

		monitoringThread = new Thread(new Runnable() {
			@Override
			public void run() {
				int interval = AgentConfiguration.getJvmMonitoringInterval();

				while (!Thread.interrupted()) {
					writeMeasure();
					try {
						Thread.sleep(interval);
					} catch (InterruptedException e) {
						break;
					}
				}
			}
		}, "JvmMonitoring thread");

		monitoringThread.setDaemon(true);

		pid = fillPid();
		fillIps();

		gc2 = null;
		gc1 = null;

		for (Object item : gcMBeans) {
			GarbageCollectorMXBean bean = (GarbageCollectorMXBean) item;

			for (String gens : bean.getMemoryPoolNames()) {
				if (gens.toLowerCase().contains("old") && gc1 == null) {
					gc1 = bean;
					break;
				}
			}

			if (gc1 != null) {
				gc2 = bean;
				break;
			}
		}

	}

	private void writeMeasure() {
		try {
			data.getWriteLock().lock();
			JvmInfo info = data.getItemToWrite();
			info.freePhysMem = osMBean.getFreePhysicalMemorySize();
			info.freeSwap = osMBean.getFreeSwapSpaceSize();
			info.loadedClassCount = classLoadingMXBean.getLoadedClassCount();
			info.peakThreadCount = threadMBean.getPeakThreadCount();
			info.pid = pid;
			info.processCpuLoad = osMBean.getProcessCpuLoad();
			info.systemCpuLoad = osMBean.getSystemCpuLoad();
			info.threadCount = threadMBean.getThreadCount();
			info.threadDaemonCount = threadMBean.getDaemonThreadCount();
			info.totalPhysMem = osMBean.getTotalPhysicalMemorySize();
			info.totalSwap = osMBean.getTotalSwapSpaceSize();
			info.totalStartedThreadCount = threadMBean.getTotalStartedThreadCount();
			info.heapCommited = memoryMBean.getHeapMemoryUsage().getCommitted();
			info.heapInit = memoryMBean.getHeapMemoryUsage().getInit();
			info.heapMax = memoryMBean.getHeapMemoryUsage().getMax();
			info.heapUsed = memoryMBean.getHeapMemoryUsage().getUsed();

			if (gc1 != null) {
				info.lastTimeGc1 = gc1.getCollectionTime();
				info.totalCountGc1 = gc1.getCollectionCount();
			}

			if (gc2 != null) {
				info.lastTimeGc2 = gc2.getCollectionTime();
				info.totalCountGc2 = gc2.getCollectionCount();
			}

			info.ipv4Addr1 = ips[0];
			info.ipv4Addr2 = ips[1];
			info.ipv4Addr3 = ips[2];
			info.ipv4Addr4 = ips[3];

		} finally {
			data.getWriteLock().unlock();
		}
	}

	private long fillPid() {
		long pid = -1;
		try {
			String name = ManagementFactory.getRuntimeMXBean().getName();
			pid = Long.parseLong(name.substring(0, name.indexOf('@')));
		} catch (Exception e) {
			pid = -1;
		}

		return pid;
	}

	private void fillIps() {
		try {
			int ipIdx = 0;
			for (NetworkInterface netIf : Collections.list(NetworkInterface.getNetworkInterfaces())) {
				if (netIf.getInetAddresses().hasMoreElements()) {
					InetAddress addr = netIf.getInetAddresses().nextElement();

					if (ipIdx < 4) {
						ips[ipIdx++] = ByteBuffer.wrap(addr.getAddress()).getInt();
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public static final JvmMonitoring getInstance() {
		return instance;
	}

	public void start() {
		monitoringThread.start();
	}

	public void stop() throws InterruptedException {
		monitoringThread.interrupt();
		monitoringThread.join(10000);
	}

	public void doMeasureAtExit() {
		writeMeasure();
	}

	public static boolean hasMore() {
		return data.hasMore();
	}

	public static JvmInfo readData(JvmInfo info) {
		return data.readItemTo(info);
	}
}
