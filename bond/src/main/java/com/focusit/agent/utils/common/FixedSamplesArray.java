package com.focusit.agent.utils.common;

import com.focusit.agent.metrics.samples.Sample;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Common code to work with limited static arrays of measured samples
 * Use with careful
 * <p/>
 * Created by Denis V. Kirpichenkov on 10.12.14.
 */
public class FixedSamplesArray<T> {
	private final Sample<T> data[];
	private final int limit;

	/** items index for next take, poll, peek or remove */
	int takeIndex=0;

	/** Number of elements in the queue */
	int count;

	/** items index for next put, offer, or add */
	int putIndex=0;

//	private final AtomicInteger position = new AtomicInteger(0);
	private final ReentrantLock lock = new ReentrantLock(false);
	private final Condition notEmpty;
	private final Condition notFull;

	private final int batchSize;

	private final String name;

	public FixedSamplesArray(int limit, ItemInitializer creator, String name, int batchSize) {
		this.limit = limit;
		this.name = name;
		this.batchSize = batchSize;
		notEmpty = lock.newCondition();
		notFull =  lock.newCondition();

		// backing array initialization
		data = creator.initData(limit);

		for (int i = 0; i < limit; i++) {
			data[i] = creator.createItem();
		}
	}

	/**
	 * Get direct data item by current index.
	 * use always with locking!
	 * <p/>
	 * Method is useful when need to manually edit item within array and when there is no way to simply copy item's data
	 *
	 * @return
	 */
	public void writeItemFrom(long ... fields) throws InterruptedException {
//		if (isFull()) {
//			System.err.println("No memory to dump sample in " + name);
////			return;
//		}
		InterruptedException interrupted = null;
		final Lock lock = this.lock;
		final Sample<T>[] data = this.data;
		try {
			try {
				lock.lockInterruptibly();
			} catch (InterruptedException e){
				interrupted = e;
				return;
			}
			while (count == data.length)
				notFull.await();
			Sample<T> result = data[putIndex];
			if (++putIndex == data.length)
				putIndex = 0;
			result.readFromBuffer(fields);
			count++;

			if(count>batchSize)
				notEmpty.signal();

		} finally {
			if(interrupted==null)
				lock.unlock();
			else
				throw interrupted;
		}
	}

	/**
	 * Get identifier of this data array. Used to log some events
	 *
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Oridnal write item to array. Data written through copying sample's data to the array.
	 *
	 * @param itemToCopyFrom
	 */
	public void writeItemFrom(Sample<T> itemToCopyFrom) throws InterruptedException {
//		if (isFull()) {
//			System.err.println("No memory to dump sample in " + name);
//		}
		InterruptedException interrupted = null;
		final Lock lock = this.lock;
		final Sample<T>[] data = this.data;
		try {
			try {
				lock.lockInterruptibly();
			} catch (InterruptedException e){
				interrupted = e;
				return;
			}
			while (count == data.length)
				notFull.await();
			Sample<T> result = data[putIndex];
			if (++putIndex == data.length)
				putIndex = 0;
			result.copyDataFrom(itemToCopyFrom);
			count++;

			if(count>batchSize)
				notEmpty.signal();
		} finally {
			if(interrupted==null)
				lock.unlock();
			else
				throw interrupted;
		}
	}

	public T readItemTo(Sample<T> itemToReadTo) throws InterruptedException {
//		if (isEmpty()) {
//			System.err.println("No samples to read in " + name);
//		}
		InterruptedException interrupted = null;
		final Lock lock = this.lock;
		final Sample<T>[] data = this.data;
		try {
			try {
				lock.lockInterruptibly();
			} catch (InterruptedException e){
				interrupted = e;
				return null;
			}
			while (count == 0)
				notEmpty.await();
			Sample<T> result = data[takeIndex];
			itemToReadTo.copyDataFrom(result);

			if (++takeIndex == data.length)
				takeIndex = 0;
			count--;

			if(itemsLeft()>batchSize)
				notFull.signal();
			return (T)itemToReadTo;
		} finally {
			if(interrupted==null)
				lock.unlock();
			else{
				throw interrupted;
			}
		}

	}

	/**
	 * Data array custom initialization
	 * @param <T>
	 */
	public interface ItemInitializer<T> {
		/**
		 * Initialize whole array
		 * @param limit
		 * @return
		 */
		Sample<T>[] initData(int limit);

		/**
		 * Initialize each sample
		 * @return
		 */
		Sample<T> createItem();
	}

	/**
	 * Has array any data to read
	 * @return
	 */
	public boolean hasMore() throws InterruptedException {
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			return count>0;
		} finally {
			lock.unlock();
		}
	}

	public int itemsLeft() throws InterruptedException {
		final ReentrantLock lock = this.lock;
		lock.lockInterruptibly();
		try {
			return data.length - count;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Is there any data to read
	 *
	 * @return
	 */
	public boolean isEmpty() throws InterruptedException {
		return !hasMore();
	}

	/**
	 * Is there a array overflow
	 *
	 * @return
	 */
	public boolean isFull() throws InterruptedException {
		return itemsLeft()==0;
	}
}
