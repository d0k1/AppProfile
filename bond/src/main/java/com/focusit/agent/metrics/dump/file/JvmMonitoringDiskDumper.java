package com.focusit.agent.metrics.dump.file;

import com.focusit.agent.metrics.JvmMonitoring;
import com.focusit.agent.metrics.dump.SamplesDataDumper;
import com.focusit.agent.metrics.samples.JvmInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Jvm monitoring data disk writer
 * <p/>
 * Created by Denis V. Kirpichenkov on 10.12.14.
 */
public class JvmMonitoringDiskDumper implements SamplesDataDumper {
	private static final Logger LOG = LoggerFactory.getLogger(StatisticDiskDumper.class);
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
							Thread.sleep(10);
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
				LOG.error("Error jvm monitoring dump", e);
			}
		} finally {
			readWriteLock.readLock().unlock();
		}
	}

	@Override
	public final void dumpRest() {
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
		dumper.join(10000);
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
