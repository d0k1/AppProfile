package com.focusit.agent.bond.metrics;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

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

	public MethodsMapDumper(String file) throws FileNotFoundException {
		aFile = new RandomAccessFile(file, "rw");
		channel = aFile.getChannel();

		dumper = new Thread(new Runnable() {
			@Override
			public void run() {
				try{
					while (!Thread.interrupted()) {

						try {
							Thread.sleep(10);

							while(lastIndex<map.getLastIndex()){
								doDump();
							}

						} catch (InterruptedException e) {
							break;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} finally {
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

	public void exit() throws InterruptedException {
		dumper.join(1000);
	}

	public void doDump() throws IOException {
		byte bytes[] = map.getMethod((int) lastIndex).getBytes(cs);
		channel.write(ByteBuffer.wrap(bytes, 0, bytes.length+1));
		lastIndex++;
	}

	public void start() {
		dumper.start();
	}
}
