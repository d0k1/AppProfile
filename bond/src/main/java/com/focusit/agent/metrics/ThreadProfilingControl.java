package com.focusit.agent.metrics;

import com.focusit.agent.metrics.samples.ProfilingInfo;
import com.focusit.agent.utils.common.BondLongObjectMap;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Denis V. Kirpichenkov on 20.02.15.
 */
public class ThreadProfilingControl {
	public final ReentrantLock lock = new ReentrantLock(true);
	public final Condition condition = lock.newCondition();
	public boolean useLock = false;
	public final BondLongObjectMap<ProfilingInfo> roots = new BondLongObjectMap<>(1000);
	public final LinkedList<ProfilingInfo> stack = new LinkedList<>();
	public ProfilingInfo current = null;
}
