package com.focusit.utils.metrics.store.file;

import com.focusit.utils.metrics.Statistics;
import com.focusit.utils.metrics.samples.ExecutionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Simple profiling data dumper. Uses RandomAccessFile to backing storage
 *
 * Created by Denis V. Kirpichenkov on 26.11.14.
 */
public class StatisticDiskDumper implements com.focusit.utils.metrics.store.Storage {
	private static final Logger LOG = LoggerFactory.getLogger(StatisticDiskDumper.class);
	private static int sampleSize = ExecutionInfo.sizeOf();
	private final int samples = 1;
	private final Thread dumper;
	private final ByteBuffer bytes = ByteBuffer.allocate((int) (samples * sampleSize));
	private final LongBuffer buffer = bytes.asLongBuffer();
	private final RandomAccessFile aFile;
	private final FileChannel channel;
	private final ExecutionInfo info = new ExecutionInfo();
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

	public StatisticDiskDumper(String file) throws FileNotFoundException {
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
		});
	}

	private void doDump() {
		try {
			readWriteLock.readLock().lock();
			for (int i = 0; i < samples; i++) {
				buffer.rewind();
				Statistics.readData(info);
				info.writeToLongBuffer(buffer);
			}
			try {
				channel.write(bytes);
				bytes.clear();
			} catch (IOException e) {
				LOG.error("Error statistics dump", e);
			}
		}finally{
			readWriteLock.readLock().unlock();
		}
	}

	@Override
	public final void dumpRest() {
		while(Statistics.hasMore()){
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
}
