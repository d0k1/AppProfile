package com.focusit.agent.metrics;

import com.focusit.agent.metrics.samples.ThreadCallStat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Denis V. Kirpichenkov on 20.02.15.
 */
public class ThreadControl {
	public final ReentrantLock lock = new ReentrantLock(true);
	public final Map<Long, ThreadCallStat> stat = new ConcurrentHashMap<>();
}
