package com.focusit.utils.common;

import com.focusit.utils.metrics.samples.Sample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Common code to work with limited static arrays of measured samples
 * <p/>
 * Created by Denis V. Kirpichenkov on 10.12.14.
 */
public class FixedSamplesArray<T> {
	private final Sample<T> data[];
	private final int limit;
	private final AtomicInteger position = new AtomicInteger(0);
	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock(true);
	private final String name;

	private static final Logger LOG = LoggerFactory.getLogger(FixedSamplesArray.class);

	public FixedSamplesArray(int limit, ItemInitializer creator, String name) {
		this.limit = limit;
		this.name = name;

		data = creator.initData();

		for (int i = 0; i < limit; i++) {
			data[i] = creator.createItem();
		}
	}

	public void getWriteItem(Sample<T> itemToCopyFrom) {
		if (position.get() == limit) {
			LOG.error("Samples array {} is full. Skipping sample!", name);
		}

		try {
			rwLock.writeLock().lock();

			Sample<T> result = data[position.getAndIncrement()];
			result.copyDataFrom(itemToCopyFrom);

			LOG.trace("stored sample to {}" + result, name);
		} finally {
			rwLock.writeLock().unlock();
		}
	}

	public Sample<T> getItemToRead(Sample<T> itemToReadTo) {
		if (position.get() == 0) {
			LOG.error("Samples array {} is empty. Can't read", name);
			return null;
		}

		try {
			rwLock.readLock().lock();
			Sample<T> result = data[position.decrementAndGet()];
			itemToReadTo.copyDataFrom(result);
			return itemToReadTo;
		} finally {
			rwLock.readLock().unlock();
		}

	}

	public interface ItemInitializer<T> {
		Sample<T>[] initData();

		Sample<T> createItem();
	}

	public boolean hasMore() {
		return position.get() > 0;
	}
}
