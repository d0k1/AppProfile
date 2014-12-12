package com.focusit.agent.metrics.dump.file;

import com.focusit.agent.metrics.MethodsMap;
import com.focusit.agent.metrics.dump.SamplesDataDumper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * Simple method map dumper. Uses RandomAccessFile as it's backing storage
 *
 * Created by Denis V. Kirpichenkov on 27.11.14.
 */
public class MethodsMapDiskDumper implements SamplesDataDumper {
	private static final Logger LOG = Logger.getLogger(MethodsMapDiskDumper.class.getName());
	public static final String METHOD_MAP_DUMPING_THREAD = "MethodMap dumping thread";
	private long lastIndex = 0;
	private final RandomAccessFile aFile;
	private final FileChannel channel;
	private final Thread dumper;
	private final MethodsMap map = MethodsMap.getInstance();
	private final Charset cs = Charset.forName("UTF-8");
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

	private AtomicLong samplesRead = new AtomicLong(0L);

	public MethodsMapDiskDumper(String file) throws FileNotFoundException {
		aFile = new RandomAccessFile(file, "rw");
		channel = aFile.getChannel();

		dumper = new Thread(new Runnable() {
			@Override
			public void run() {
				try{
					channel.position(0);
					while (!Thread.interrupted()) {

						try {
							while(lastIndex<map.getLastIndex()){
								doDump();
							}
							Thread.sleep(10);

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

	@Override
	public final void exit() throws InterruptedException {
		dumper.interrupt();
		dumper.join(10000);
	}

	@Override
	public final void dumpRest() {
		while(lastIndex<map.getLastIndex()){
			doDump();
		}
		try {
			channel.close();
			aFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void doDump() {
		try {
			readWriteLock.readLock().lock();

			if (lastIndex >= map.getLastIndex())
				return;

			byte bytes[] = map.getMethod((int) lastIndex).getBytes(cs);
			int length = bytes.length;
			try {

				channel.write(ByteBuffer.wrap(bytes, 0, length));
				channel.position(channel.position() + 1);
				lastIndex++;
				samplesRead.incrementAndGet();
			} catch (IOException e) {
				LOG.severe("Error method map dumping " + e.getMessage());
			}
		}finally{
			readWriteLock.readLock().unlock();
		}
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
		return METHOD_MAP_DUMPING_THREAD;
	}
}
