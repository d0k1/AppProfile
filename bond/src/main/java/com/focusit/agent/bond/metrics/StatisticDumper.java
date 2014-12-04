package com.focusit.agent.bond.metrics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Denis V. Kirpichenkov on 26.11.14.
 */
public class StatisticDumper {
	private int samples = 1;
	private static int sampleSize = ExecutionInfo.sizeOf();

	private Thread dumper;
	private ByteBuffer bytes = ByteBuffer.allocate((int) (samples*sampleSize));
	private LongBuffer buffer = bytes.asLongBuffer();
	private RandomAccessFile aFile;
	private FileChannel channel;
	private ExecutionInfo info = new ExecutionInfo();
	private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

	public StatisticDumper(String file) throws FileNotFoundException {
		aFile = new RandomAccessFile(file, "rw");
		channel = aFile.getChannel();
		dumper = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (!Thread.interrupted()) {
						try {
							Thread.sleep(10);
							doDump();
						} catch (InterruptedException e) {
							break;
						}
					}
				}finally {
				}
			}
		});
	}

	public void doDump(){
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
				System.err.println(e.getMessage());
			}
		}finally{
			readWriteLock.readLock().unlock();
		}
	}

	public void dumpRest(){
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

	public void exit() throws InterruptedException {
		dumper.interrupt();
		dumper.join(10000);
	}

	public void start(){
		dumper.start();
	}
}
