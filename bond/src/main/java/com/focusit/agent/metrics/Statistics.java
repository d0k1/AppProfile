package com.focusit.agent.metrics;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.bond.time.GlobalTime;
import com.focusit.agent.metrics.samples.ExecutionInfo;
import com.focusit.agent.metrics.samples.Sample;
import com.focusit.agent.utils.common.FixedSamplesArray;
import com.focusit.agent.utils.jmm.FinalBoolean;

/**
 * Class to dump profiling data to it's own temporary buffer.
 * Will Log a lot if buffer is full
 * Created by Denis V. Kirpichenkov on 26.11.14.
 */
public class Statistics {
	// Max samples in memory - 6 553 600 * com.focusit.agent.metrics.samples.ExecutionInfo.sizeOf() = 6553600 * 32 = 209 715 200 = 200 Mb
	private final static int LIMIT = AgentConfiguration.getStatisticsBufferLength();
	private final static long appId = AgentConfiguration.getAppId();
	public static FinalBoolean enabled = new FinalBoolean(AgentConfiguration.isStatisticsEnabled());

	private static final FixedSamplesArray<ExecutionInfo> data = new FixedSamplesArray<>(LIMIT, new FixedSamplesArray.ItemInitializer() {
		@Override
		public Sample[] initData(int limit) {
			return new ExecutionInfo[limit];
		}

		@Override
		public Sample createItem() {
			return new ExecutionInfo();
		}
	}, "Statistics", AgentConfiguration.getStatisticsDumpBatch());

	public static void storeEnter(long methodId) throws InterruptedException {
		FinalBoolean working = enabled;

		if(!working.value)
			return;

		data.writeItemFrom(Thread.currentThread().getId(), 0, GlobalTime.getCurrentTime(), methodId, GlobalTime.getCurrentTimeInMillis(), appId);
	}

	public static void storeLeave(long methodId) throws InterruptedException {
		FinalBoolean working = enabled;

		if(!working.value)
			return;

		data.writeItemFrom(Thread.currentThread().getId(), 1, GlobalTime.getCurrentTime(), methodId, GlobalTime.getCurrentTimeInMillis(), appId);
	}

	public static void storeLeaveException(long methodId) throws InterruptedException {
		FinalBoolean working = enabled;

		if(!working.value)
			return;

		data.writeItemFrom(Thread.currentThread().getId(), 2, GlobalTime.getCurrentTime(), methodId, GlobalTime.getCurrentTimeInMillis(), appId);
	}

	public static ExecutionInfo readData(ExecutionInfo info) throws InterruptedException {
		return data.readItemTo(info);
	}

	public static boolean hasMore() throws InterruptedException {
		return data.hasMore();
	}
}
