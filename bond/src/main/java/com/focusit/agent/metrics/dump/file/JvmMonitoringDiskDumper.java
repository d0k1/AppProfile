package com.focusit.agent.metrics.dump.file;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.JvmMonitoring;
import com.focusit.agent.metrics.dump.SamplesDataDumper;
import com.focusit.agent.metrics.samples.JvmInfo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
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
	private final int samples = 1;
	private final Thread dumper;
	private final ByteBuffer bytes = ByteBuffer.allocate((int) (samples * sampleSize));
	private final LongBuffer buffer = bytes.asLongBuffer();
	private final RandomAccessFile aFile;
	private final FileChannel channel;
	private final JvmInfo info = new JvmInfo();
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

	private AtomicLong samplesRead = new AtomicLong(0L);

	public JvmMonitoringDiskDumper(String file) throws FileNotFoundException {
		aFile = new RandomAccessFile(file, "rw");
		channel = aFile.getChannel();

		dumper = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					channel.position(0);
					while (!Thread.interrupted()) {
						try {
							Thread.sleep(AgentConfiguration.getDumpInterval());
							doDump();
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
	}

	private void doDump() {

		if (!JvmMonitoring.hasMore())
			return;

		try {
			readWriteLock.readLock().lock();
			for (int i = 0; i < samples; i++) {
				buffer.rewind();
				JvmMonitoring.readData(info);
				info.writeToLongBuffer(buffer);
				samplesRead.incrementAndGet();
			}
			try {
				channel.write(bytes);
				bytes.clear();

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
			aFile.close();
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
