package com.focusit.agent.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Array that stores method links.
 * 
 * Created by Denis V. Kirpichenkov on 26.11.14.
 */
public class MethodsMap {
	private static final int INITIAL_SIZE = 1500000;
	private static final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
	private static final List<String> methods = new ArrayList<>(INITIAL_SIZE);
	private static final ConcurrentHashMap<String, Long> methodIndexes = new ConcurrentHashMap<>(INITIAL_SIZE);;
	private static final AtomicLong lastIndex = new AtomicLong(0);

	public static long addMethod(String method){
		try {
			rwLock.writeLock().lock();

			Long current = methodIndexes.get(method);

			if (current != null) {
				return current;
			}

			methods.add(method);
			long index = lastIndex.getAndIncrement();
			methodIndexes.put(method, index);

			//LOG.fine("mappedMethod: " + method + " = " + index);
			return index;
		} finally{
			rwLock.writeLock().unlock();
		}
	}

	public static long getMethodIndex(String method){
		try{
			rwLock.readLock().lock();

			Long result = methodIndexes.get(method);
			return result==null?-1:result;
		} finally {
			rwLock.readLock().unlock();
		}
	}

	public static long getLastIndex(){
		return lastIndex.get();
	}

	public static String getMethod(int index){
		try{
			rwLock.readLock().lock();

			return methods.get(index);
		} finally {
			rwLock.readLock().unlock();
		}
	}
}
