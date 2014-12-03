package com.focusit.agent.bond.metrics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.channels.FileChannel;

/**
 * Created by Denis V. Kirpichenkov on 26.11.14.
 */
public class StatisticDumper {
	private int samples = 1;

	private Thread dumper;
	private ByteBuffer bytes = ByteBuffer.allocate((int) (samples*ExecutionInfo.sizeOf()));
	private LongBuffer buffer = bytes.asLongBuffer();
	private RandomAccessFile aFile;
	private FileChannel channel;
	private ExecutionInfo info = new ExecutionInfo();

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
					try {
						channel.close();
						aFile.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	public void doDump(){
		for (int i = 0; i < samples; i++) {
			Statistics.readData(info);
			info.writeToLongBuffer(buffer);
		}
		try {
			bytes.flip();
			channel.write(bytes);
			buffer.clear();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void exit() throws InterruptedException {
		dumper.join(1000);
	}

	public void start(){
		dumper.start();
	}
}
