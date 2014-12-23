package com.focusit.agent.metrics.dump.file;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.Statistics;
import com.focusit.agent.metrics.dump.SamplesDataDumper;
import com.focusit.agent.metrics.samples.ExecutionInfo;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple profiling data dumper. Uses RandomAccessFile to backing storage
 *
 * Created by Denis V. Kirpichenkov on 26.11.14.
 */
public class StatisticDiskDumper implements SamplesDataDumper {
	public static final String PROFILING_STAT_DUMPING_THREAD = "Profiling stat dumping thread";
	private static int sampleSize = ExecutionInfo.sizeOf();
	private final int samples = AgentConfiguration.getStatisticsDumpBatch();
	private final Thread dumper;
	private final ByteBuffer bytesBuffers[] = new ByteBuffer[samples];

	private final RandomAccessFile aFile;
	private final FileChannel channel;
	private final ExecutionInfo info = new ExecutionInfo();

	private AtomicLong samplesRead = new AtomicLong(0L);

	public StatisticDiskDumper() throws IOException {
		aFile = new RandomAccessFile(AgentConfiguration.getStatisticsFile(), "rw");
		channel = aFile.getChannel();
		channel.truncate(0);

		for(int i=0;i<samples;i++){
			bytesBuffers[i] = ByteBuffer.allocate(sampleSize);
		}

		dumper = new Thread(new Runnable() {
			@Override
			public void run() {
				int interval = AgentConfiguration.getDumpInterval();
				while (!Thread.interrupted()) {
					try {
						doDump(false);
						Thread.yield();
					} catch (InterruptedException e) {
						break;
					}
				}

			}
		}, getName());

		dumper.setDaemon(true);
//		dumper.setPriority(Thread.MAX_PRIORITY);
	}

	private void doDump(boolean checkAvailability) throws InterruptedException {

		int sampleRead = 0;
		InterruptedException interrupted = null;

		for (int i = 0; i < samples; i++) {

			if(checkAvailability) {
				if (!Statistics.hasMore())
					break;
			}

			bytesBuffers[i].clear();
			try {
				Statistics.readData(info);
				info.writeToBuffer(bytesBuffers[i]);
				samplesRead.incrementAndGet();
				bytesBuffers[i].flip();
				sampleRead++;
			} catch (InterruptedException e){
				interrupted = e;
				break;
			}
		}

		try {
			if(sampleRead>0) {
				channel.write(bytesBuffers, 0, sampleRead);
			}
		} catch (IOException e) {
			System.err.println("Error statistics dump " + e);
		}

		if(interrupted!=null) {
			throw interrupted;
		}
	}

	@Override
	public final void dumpRest() throws InterruptedException {
		while(Statistics.hasMore()){
			doDump(true);
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
		return PROFILING_STAT_DUMPING_THREAD;
	}
}
