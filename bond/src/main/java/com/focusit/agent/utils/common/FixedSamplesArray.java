package com.focusit.agent.utils.common;

import com.focusit.agent.metrics.samples.Sample;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * Common code to work with limited static arrays of measured samples
 * Use with careful
 * <p/>
 * Created by Denis V. Kirpichenkov on 10.12.14.
 */
public class FixedSamplesArray<T> {
	private final Sample<T> data[];
	private final int limit;
	private final AtomicInteger position = new AtomicInteger(0);
	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
	private final String name;

	private static final Logger LOG = Logger.getLogger(FixedSamplesArray.class.getName());

	public FixedSamplesArray(int limit, ItemInitializer creator, String name) {
		this.limit = limit;
		this.name = name;

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
	public T getItemToWrite() {
		Sample<T> result = data[position.getAndIncrement()];

		//LOG.finer("stored sample to " + name + " " + result);
		return (T) result;
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
	public void writeItemFrom(Sample<T> itemToCopyFrom) {
		if (isFull()) {
			System.err.println("No memory to dump sample in " + name);
			return;
		}
		try {
			getWriteLock().lock();

			Sample<T> result = data[position.getAndIncrement()];
			result.copyDataFrom(itemToCopyFrom);

			//LOG.finer("stored sample to " + name + " " + result);
		} finally {
			getWriteLock().unlock();
		}
	}

	public T readItemTo(Sample<T> itemToReadTo) {
		if (isEmpty()) {
			System.err.println("No samples to read in " + name);
			return null;
		}

		try {
			rwLock.readLock().lock();
			Sample<T> result = data[position.decrementAndGet()];
			itemToReadTo.copyDataFrom(result);

			//LOG.finer("Read sample from " + name);
			return (T)itemToReadTo;
		} finally {
			rwLock.readLock().unlock();
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
	public boolean hasMore() {
		return position.get() > 0;
	}

	/**
	 * Is there any data to read
	 *
	 * @return
	 */
	public boolean isEmpty() {
		return !hasMore();
	}

	/**
	 * Is there a array overflow
	 *
	 * @return
	 */
	public boolean isFull() {
		return position.get() == limit;
	}

	/**
	 * Get write lock to use with manual data writing.
	 * @see FixedSamplesArray#getItemToWrite()
	 * @return
	 */
	public Lock getWriteLock(){
		return rwLock.writeLock();
	}
}
