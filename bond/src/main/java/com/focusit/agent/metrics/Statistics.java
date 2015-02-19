package com.focusit.agent.metrics;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.bond.time.GlobalTime;
import com.focusit.agent.metrics.samples.ThreadCallStat;
import com.focusit.agent.utils.jmm.FinalBoolean;

import java.util.Map;

/**
 * Class to dump profiling data to it's own temporary buffer.
 * Will Log a lot if buffer is full
 * Created by Denis V. Kirpichenkov on 26.11.14.
 */
public class Statistics {
	public static FinalBoolean enabled = new FinalBoolean(AgentConfiguration.isStatisticsEnabled());
	private static ThreadLocal<ThreadControl> threadStat = new ThreadLocal<>();

	public static void storeEnter(long methodId) throws InterruptedException {
		FinalBoolean working = enabled;

		if(!working.value)
			return;

		ThreadControl control = threadStat.get();

		control.lock.lockInterruptibly();

		try {
			if (control == null) {
				control = ThreadStatHolder.getInstance().getThreadControl();
				threadStat.set(control);
			}

			Map<Long, ThreadCallStat> methods = control.stat;

			ThreadCallStat stat = methods.get(methodId);
			if (stat == null) {
				stat = new ThreadCallStat();
				methods.put(methodId, stat);
				stat.methodId = (int) methodId;
				stat.threadId = Thread.currentThread().getId();
				stat.count = 0;
				stat.totalTime = 0;
				stat.maxTime = Long.MIN_VALUE;
				stat.minTime = Long.MAX_VALUE;
			}

			stat.count++;
			stat.stack.push(GlobalTime.getCurrentTime());
		} finally {
			control.lock.unlock();
		}
	}

	private static void updateStatOnLeave(ThreadCallStat stat, boolean exception) throws InterruptedException {

		threadStat.get().lock.lockInterruptibly();

		try {

			if (stat == null || stat.stack.size() == 0) {
				return;
			}

			Long leave = GlobalTime.getCurrentTime();
			Long time = leave - stat.stack.pop();

			if (stat.minTime > time) {
				stat.minTime = time;
			}

			if (stat.maxTime < time) {
				stat.maxTime = time;
			}

			stat.totalTime += time;
			if (exception) {
				stat.exceptions += 1;
			}
		} finally {
			threadStat.get().lock.unlock();
		}
	}

	public static void storeLeave(long methodId) throws InterruptedException {
		FinalBoolean working = enabled;

		if(!working.value)
			return;

		updateStatOnLeave(threadStat.get().stat.get(methodId), false);
	}

	public static void storeLeaveException(long methodId) throws InterruptedException {
		FinalBoolean working = enabled;

		if(!working.value)
			return;

		updateStatOnLeave(threadStat.get().stat.get(methodId), true);
	}
}
