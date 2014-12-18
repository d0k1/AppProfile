package com.focusit.agent.metrics.dump.file;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.MethodsMap;
import com.focusit.agent.metrics.dump.SamplesDataDumper;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Simple method map dumper. Uses RandomAccessFile as it's backing storage
 *
 * Created by Denis V. Kirpichenkov on 27.11.14.
 */
public class MethodsMapDiskDumper implements SamplesDataDumper {
	public static final String METHOD_MAP_DUMPING_THREAD = "MethodMap dumping thread";
	private long lastIndex = 0;
	private final RandomAccessFile aFile;
	private final FileChannel channel;
	private final Thread dumper;
	private final MethodsMap map = MethodsMap.getInstance();
	private final Charset cs = Charset.forName("UTF-8");
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

	private AtomicLong samplesRead = new AtomicLong(0L);

	public MethodsMapDiskDumper() throws IOException {
		aFile = new RandomAccessFile(AgentConfiguration.getMethodsMapFile(), "rw");
		channel = aFile.getChannel();
		channel.truncate(0);
		dumper = new Thread(new Runnable() {
			@Override
			public void run() {
				try{
					channel.position(0);
					int interval = AgentConfiguration.getDumpInterval();
					while (!Thread.interrupted()) {

						try {
							while(lastIndex<map.getLastIndex()){
								doDump();
							}
							Thread.sleep(interval);

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
//		dumper.setPriority(Thread.MAX_PRIORITY);
	}

	@Override
	public final void exit() throws InterruptedException {
		dumper.interrupt();
		dumper.join(AgentConfiguration.getThreadJoinTimeout());
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
				System.err.println("Error method map dumping " + e.getMessage());
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
