package com.focusit.agent.metrics;

import com.focusit.agent.metrics.samples.ExecutionInfo;
import com.focusit.agent.metrics.samples.Sample;
import com.focusit.agent.utils.common.FixedSamplesArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to dump profiling data to it's own temporary buffer.
 * Will Log a lot if buffer is full
 * Created by Denis V. Kirpichenkov on 26.11.14.
 */
public class Statistics {
	private static final Logger LOG = LoggerFactory.getLogger(Statistics.class);
	// Max samples in memory - 6 553 600 * com.focusit.agent.metrics.samples.ExecutionInfo.sizeOf() = 6553600 * 32 = 209 715 200 = 200 Mb
	private final static int LIMIT = 6553600;

	private static final FixedSamplesArray<ExecutionInfo> data = new FixedSamplesArray<>(LIMIT, new FixedSamplesArray.ItemInitializer() {
		@Override
		public Sample[] initData(int limit) {
			return new ExecutionInfo[limit];
		}

		@Override
		public Sample createItem() {
			return new ExecutionInfo();
		}
	}, "Statistics");

	public static void storeData(long methodId, long start, long stop) {

		if (data.isFull()) {
			LOG.error("No memory to dump sample in {}", data.getName());
			return;
		}
		try {

			data.getWriteLock().lock();
			ExecutionInfo result = data.getItemToWrite();
			result.threadId = Thread.currentThread().getId();
			result.method = methodId;
			result.start = start;
			result.end = stop;

			LOG.trace("stored method call " + result);
		} finally {
			data.getWriteLock().unlock();
		}
	}

	public static ExecutionInfo readData(ExecutionInfo info) {
		return data.readItemTo(info);
	}

	public static boolean hasMore() {
		return data.hasMore();
	}
}
