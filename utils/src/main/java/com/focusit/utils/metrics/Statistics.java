package com.focusit.utils.metrics;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by Denis V. Kirpichenkov on 26.11.14.
 */
public class Statistics {
	private final static int LIMIT=1638400;
	public static ExecutionInfo []data = new ExecutionInfo[LIMIT];
	private final static AtomicInteger position = new AtomicInteger(0);
	private final static ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);

	static {
		for(int i=0;i<LIMIT;i++){
			data[i] = new ExecutionInfo();
		}
	}

	public static ExecutionInfo storeData(long methodId, long start, long stop){
		if(position.get()==LIMIT)
			return null;

		try {
			rwLock.writeLock().lock();

			ExecutionInfo result = data[position.getAndIncrement()];
			result.threadId = Thread.currentThread().getId();
			result.method = methodId;
			result.start = start;
			result.end = stop;

//			System.err.println("Method call: "+result);
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
