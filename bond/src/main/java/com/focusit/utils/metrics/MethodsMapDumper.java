package com.focusit.utils.metrics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Denis V. Kirpichenkov on 27.11.14.
 */
public class MethodsMapDumper {
	private long lastIndex = 0;
	private RandomAccessFile aFile;
	private FileChannel channel;
	private Thread dumper;
	private MethodsMap map = MethodsMap.getInstance();
	private Charset cs = Charset.forName("UTF-8");
	private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);


	public MethodsMapDumper(String file) throws FileNotFoundException {
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
		});
	}

	public void exit() throws InterruptedException {
		dumper.interrupt();
		dumper.join(10000);
	}

	public void dumpRest(){
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

	public void doDump() {
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
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}finally{
			readWriteLock.readLock().unlock();
		}
	}

	public void start() {
		dumper.start();
	}
}
