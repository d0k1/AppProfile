package com.focusit.agent.metrics;

import com.focusit.agent.metrics.samples.ProfilingInfo;
import com.focusit.agent.utils.common.LongObjectRedBlackTree;

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
	public final LongObjectRedBlackTree<ProfilingInfo> roots = new LongObjectRedBlackTree<>();
	public final LinkedList<ProfilingInfo> stack = new LinkedList<>();
	public ProfilingInfo current = null;
}
