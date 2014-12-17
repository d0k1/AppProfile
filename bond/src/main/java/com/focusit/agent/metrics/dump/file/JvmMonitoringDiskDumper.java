package com.focusit.agent.metrics.dump.file;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.JvmMonitoring;
import com.focusit.agent.metrics.dump.SamplesDataDumper;
import com.focusit.agent.metrics.samples.JvmInfo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.StandardOpenOption;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * Jvm monitoring data disk writer
 * <p/>
 * Created by Denis V. Kirpichenkov on 10.12.14.
 */
public class JvmMonitoringDiskDumper implements SamplesDataDumper {
	private static final Logger LOG = Logger.getLogger(JvmMonitoringDiskDumper.class.getName());
	public static final String JVM_MONITORING_DUMPING_THREAD = "Jvm monitoring dumping thread";
	private static int sampleSize = JvmInfo.sizeOf();
	private final int samples = 500;
	private final Thread dumper;
	private final ByteBuffer bytesBuffers[] = new ByteBuffer[samples];

	private final FileChannel channel;
	private final JvmInfo info = new JvmInfo();
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

	private AtomicLong samplesRead = new AtomicLong(0L);

	public JvmMonitoringDiskDumper(String file) throws IOException {
		channel = FileChannel.open(FileSystems.getDefault().getPath(file), EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));

		for(int i=0;i<samples;i++){
			bytesBuffers[i] = ByteBuffer.allocate(sampleSize);
		}

		dumper = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					channel.position(0);
					int interval = AgentConfiguration.getDumpInterval();
					while (!Thread.interrupted()) {
						try {
							Thread.sleep(0, interval);
							while (JvmMonitoring.hasMore()) {
								doDump();
							}
						} catch (InterruptedException e) {
							break;
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
				}
			}
		}, getName());
		dumper.setDaemon(true);
		dumper.setPriority(Thread.MAX_PRIORITY);
	}

	private void doDump() {

		boolean hasMore = JvmMonitoring.hasMore();
		if (!hasMore)
			return;

		try {
			readWriteLock.readLock().lock();
			int sampleRead = 0;
			for (int i = 0; i < samples && hasMore; i++) {
				bytesBuffers[i].clear();
				JvmMonitoring.readData(info);
				info.writeToBuffer(bytesBuffers[i]);
				samplesRead.incrementAndGet();
				hasMore = JvmMonitoring.hasMore();
				bytesBuffers[i].flip();
				sampleRead++;
			}
			try {
				channel.write(bytesBuffers, 0, sampleRead);
			} catch (IOException e) {
				System.err.println("Error jvm monitoring dump " + e.getMessage());
			}
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	@Override
	public final void dumpRest() {
		JvmMonitoring.getInstance().doMeasureAtExit();

		while (JvmMonitoring.hasMore()) {
			doDump();
		}
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public final void exit() throws InterruptedException {
		dumper.interrupt();
		dumper.join(AgentConfiguration.getThreadJoinTimeout());
	}

	@Override
	public final void start() {
		dumper.start();
	}

	@Override
	public long getSamplesRead() {
		return samplesRead.get();
	}

	@Override
	public String getName() {
		return JVM_MONITORING_DUMPING_THREAD;
	}
}
