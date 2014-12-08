package com.focusit.utils.metrics;

import com.focusit.utils.metrics.samples.ExecutionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class to store profiling data to it's own temporary buffer.
 * Will Log a lot if buffer is full
 * Created by Denis V. Kirpichenkov on 26.11.14.
 */
public class Statistics {
	private static final Logger LOG = LoggerFactory.getLogger(Statistics.class);
	// Max samples in memory - 6 553 600 * com.focusit.utils.metrics.samples.ExecutionInfo.sizeOf() = 6553600 * 32 = 209 715 200 = 200 Mb
	private final static int LIMIT = 6553600;
	private final static AtomicInteger position = new AtomicInteger(0);
	private final static ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);

	private static final ExecutionInfo[] data = new ExecutionInfo[LIMIT];

	static {
		for(int i=0;i<LIMIT;i++){
			data[i] = new ExecutionInfo();
		}
	}

	public static ExecutionInfo storeData(long methodId, long start, long stop){
		if (position.get() == LIMIT) {
			LOG.error("Profiling buffer is full. Skipping samples!");
			return null;
		}

		try {
			rwLock.writeLock().lock();

			ExecutionInfo result = data[position.getAndIncrement()];
			result.threadId = Thread.currentThread().getId();
			result.method = methodId;
			result.start = start;
			result.end = stop;

			LOG.trace("stored method call " + result);
			return result;
		} finally {
			rwLock.writeLock().unlock();
		}
	}

	public static ExecutionInfo readData(ExecutionInfo info){

		if(position.get()==0)
			return null;

		try{
			rwLock.readLock().lock();
			ExecutionInfo result = data[position.decrementAndGet()];

			info.threadId = result.threadId;
			info.method = result.method;
			info.end = result.end;
			info.start = result.start;

			return info;
		} finally {
			rwLock.readLock().unlock();
		}
	}

	public static boolean hasMore(){
		return position.get()>0;
	}
}
