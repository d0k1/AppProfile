package com.focusit.agent.metrics;

import com.focusit.agent.metrics.samples.ProfilingInfo;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Denis V. Kirpichenkov on 20.02.15.
 */
public class ThreadProfilingControl {
	public final ReentrantLock lock = new ReentrantLock(true);
	public final Long2ObjectMap<ProfilingInfo> roots = new Long2ObjectOpenHashMap<>();
	public final ObjectArrayList<ProfilingInfo> stack = new ObjectArrayList<>();
	public ProfilingInfo current = null;
}
