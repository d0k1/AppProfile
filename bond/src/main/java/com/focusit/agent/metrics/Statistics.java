package com.focusit.agent.metrics;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.bond.time.GlobalTime;
import com.focusit.agent.metrics.samples.ProfilingInfo;
import com.focusit.agent.utils.common.BondThreadLocal;
import com.focusit.agent.utils.jmm.FinalBoolean;

/**
 * Class to dump profiling data to it's own temporary buffer.
 * Will Log a lot if buffer is full
 * Created by Denis V. Kirpichenkov on 26.11.14.
 */
public class Statistics {
	public static FinalBoolean enabled = new FinalBoolean(AgentConfiguration.isStatisticsEnabled());
	private static final BondThreadLocal<ThreadProfilingControl> threadStat = new BondThreadLocal<>();

	public static void storeEnter(long methodId) throws InterruptedException {
		long threadId = Thread.currentThread().getId();

		FinalBoolean working = enabled;

		if(!working.value)
			return;

		ThreadProfilingControl control = threadStat.get(threadId);

		if (control == null) {
			control = ProfilerDataHolder.getThreadControl();
			threadStat.put(threadId, control);
		}

		boolean locking = control.useLock;

		if(locking) {
			control.lock.lock();
			control.condition.signal();
			control.condition.await();
		}

		try {

			// Вход в метод.
			ProfilingInfo stat = control.current;

			// если еще не были внтури ниодного метода
			if (stat == null) {

				// ищем такой же, для статистики
				stat = control.roots.get(methodId);

				// если не бывали в таком методе раньше
				if(stat == null){
					stat = new ProfilingInfo();
					control.roots.put(methodId, stat);
					control.current = stat;
				} else {
					control.current = stat;
				}
			} else {
				// если уже внутри какого-то метода
				// пробуем найти метод внутри текущего, в который уже погружались
				stat = control.current.childs.get(methodId);
				// не найден - создаем новый
				if(stat==null){
					stat = new ProfilingInfo();
					control.current.childs.put(methodId, stat);
				}

				// кладем в стек потока текущий метод
				control.stack.push(control.current);
				// текущим становиться stat метод
				control.current = stat;
			}

			stat.count++;
			stat.methodId = (int) methodId;
			stat.threadId = Thread.currentThread().getId();
			stat.enterTime = GlobalTime.getCurrentTime();
		} finally {
			if(locking) {
				control.lock.unlock();
			}
		}
	}

	private static void updateStatOnLeave(boolean exception) throws InterruptedException {
		long threadId = Thread.currentThread().getId();
		ThreadProfilingControl control = threadStat.get(threadId);

		boolean locking = control.useLock;

		if(locking) {
			control.lock.lock();
			control.condition.signal();
			control.condition.await();
		}

		ProfilingInfo stat = control.current;

		try {

			if (stat == null || stat.reset) {
				return;
			}

			long leave = GlobalTime.getCurrentTime();
			long time = leave - stat.enterTime;
			stat.enterTime = -1;

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

			if(control.stack.size()>0){
				control.current = control.stack.pop();
			} else {
				control.current = null;
			}

		} finally {
			if(locking) {
				control.lock.unlock();
			}
		}
	}

	public static void storeLeave(long methodId) throws InterruptedException {
		FinalBoolean working = enabled;

		if(!working.value)
			return;

		updateStatOnLeave(false);
	}

	public static void storeLeaveException(long methodId) throws InterruptedException {
		FinalBoolean working = enabled;

		if(!working.value)
			return;

		updateStatOnLeave(true);
	}
}
