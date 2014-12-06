package com.focusit.agent.bond.string;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Utility class to store strings
 * <p/>
 * Created by Denis V. Kirpichenkov on 10.08.14.
 */
public class StringHolder {

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private long nextIndex = 0L;
	private long index = 0L;
	private HashMap<String, Long> strings = new HashMap<>();
	private HashMap<Long, String> indexes = new HashMap<>();

	public long putString(String str) {
		try {
			lock.writeLock().lock();
			Long strIndex = strings.get(str);
			if (strIndex != null)
				return strIndex.longValue();

			strings.put(str, nextIndex);
			indexes.put(nextIndex, str);

			index = nextIndex;
			nextIndex++;

			return index;
		} finally {
			lock.writeLock().unlock();
		}
	}

	public String getString(long index) {
		return indexes.get(index);
	}
}
