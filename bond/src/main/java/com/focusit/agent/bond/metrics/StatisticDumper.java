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

	private Thread dumper;
	private ByteBuffer bytes = ByteBuffer.allocate(4*64);
	private LongBuffer buffer = bytes.asLongBuffer();
	private RandomAccessFile aFile;
	private FileChannel channel;

	public StatisticDumper(String file) throws IOException {
		aFile = new RandomAccessFile("profile.data", "w");
		channel = aFile.getChannel();
		dumper = new Thread(new Runnable() {
			@Override
			public void run() {
				ExecutionInfo info = new ExecutionInfo();
				while(!Thread.interrupted()){
					try {
						Thread.sleep(500);
						Statistics.readData(info);
						info.writeToLongBuffer(buffer);
						try {
							channel.write(bytes);
							buffer.clear();
						} catch (IOException e) {
							e.printStackTrace();
						}

					} catch (InterruptedException e) {
						break;
					}
				}
			}
		});
	}

	public void start(){
		dumper.start();
	}
}
