package com.focusit.agent.metrics;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.bond.time.GlobalTime;
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
	private static final long appId = AgentConfiguration.getAppId();
	private final OperatingSystemMXBean osMBean;
	private final ThreadMXBean threadMBean;
	private final ClassLoadingMXBean classLoadingMXBean;
	private final MemoryMXBean memoryMBean;
	private GarbageCollectorMXBean gc1;
	private GarbageCollectorMXBean gc2;

	// Max LIMIT in memory = 655360  * 176 (bytes per sample) = 125 829 120 bytes = 120 Mb
	private final static int LIMIT = AgentConfiguration.getJvmBufferLength();
	private final static FixedSamplesArray<JvmInfo> data = new FixedSamplesArray<>(LIMIT, new FixedSamplesArray.ItemInitializer() {
		@Override
		public Sample[] initData(int limit) {
			return new JvmInfo[limit];
		}

		@Override
		public Sample createItem() {
			return new JvmInfo();
		}
	}, "JvmStat", AgentConfiguration.getJvmDumpBatch());

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
					try {
						writeMeasure();
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

	private void writeMeasure() throws InterruptedException {
		if (data.isFull()) {
			System.err.println("No memory to store sample in " + data.getName());
		}

		long time1 = 0;
		long time2 = 0;
		long total1 = 0;
		long total2 = 0;
		if (gc1 != null) {
			time1 = gc1.getCollectionTime();
			total1 = gc1.getCollectionCount();
		}

		if (gc2 != null) {
			time2 = gc2.getCollectionTime();
			total2 = gc2.getCollectionCount();
		}

		data.writeItemFrom(ips[0], ips[1], ips[2], ips[3], pid, classLoadingMXBean.getLoadedClassCount(), memoryMBean.getHeapMemoryUsage().getCommitted(),
			memoryMBean.getHeapMemoryUsage().getInit(), memoryMBean.getHeapMemoryUsage().getMax(),
			memoryMBean.getHeapMemoryUsage().getUsed(), osMBean.getFreePhysicalMemorySize(),
			osMBean.getFreeSwapSpaceSize(), osMBean.getTotalPhysicalMemorySize(), osMBean.getTotalSwapSpaceSize(),
			threadMBean.getThreadCount(), threadMBean.getDaemonThreadCount(), threadMBean.getPeakThreadCount(),
			threadMBean.getTotalStartedThreadCount(), time1, time2, total1, total2, Double.doubleToLongBits(osMBean.getProcessCpuLoad()),
			Double.doubleToLongBits(osMBean.getSystemCpuLoad()), GlobalTime.getCurrentTime(), GlobalTime.getCurrentTimeInMillis(), appId);
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

	public void doMeasureAtExit() throws InterruptedException {
		writeMeasure();
	}

	public static boolean hasMore() throws InterruptedException {
		return data.hasMore();
	}

	public static JvmInfo readData(JvmInfo info) throws InterruptedException {
		return data.readItemTo(info);
	}
}
