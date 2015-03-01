package com.focusit.agent.utils.common;

/**
 * Created by Denis V. Kirpichenkov on 28.02.15.
 * @NotThreadSafe
 */
public class BondThreadLocal<T> {
	private static final int POWER_OF_TWO_SIZE=128;

	private final Object buckets[][] = new Object[POWER_OF_TWO_SIZE][POWER_OF_TWO_SIZE];

	public T get(long tid){
		if(tid>16000)
			throw new IllegalArgumentException("ThreadId is too bif to store data");
		long bucket = tid / POWER_OF_TWO_SIZE;
		if(buckets[((int) bucket)]==null){
			return null;
		}
		long index = tid % POWER_OF_TWO_SIZE;
		return (T) buckets[((int) bucket)][((int) index)];
	}

	public void put(long tid, T value){
		long bucket = tid / POWER_OF_TWO_SIZE;
		if(buckets[((int) bucket)]==null){
			buckets[((int) bucket)] = new Object[POWER_OF_TWO_SIZE];
		}
		long index = tid % POWER_OF_TWO_SIZE;
		buckets[((int) bucket)][((int) index)] = value;
	}
}
