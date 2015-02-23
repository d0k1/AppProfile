package com.focusit.agent.metrics;

import com.focusit.agent.metrics.samples.ProfilingInfo;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Denis V. Kirpichenkov on 20.02.15.
 */
public class ThreadProfilingControl {
	public final ReentrantLock lock = new ReentrantLock(true);
	public final Map<Long, ProfilingInfo> roots = new ConcurrentHashMap<>();
	public final LinkedList<ProfilingInfo> stack = new LinkedList<>();
	public ProfilingInfo current = null;
}
