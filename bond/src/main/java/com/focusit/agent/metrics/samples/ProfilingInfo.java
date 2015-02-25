package com.focusit.agent.metrics.samples;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * Created by Denis V. Kirpichenkov on 15.02.15.
 */
public class ProfilingInfo {
	public long threadId = -1;
	public int methodId = -1;
	public long count = 0;
	public long totalTime = 0;
	public long exceptions = 0;
	public long minTime = Long.MAX_VALUE;
	public long maxTime = Long.MIN_VALUE;
	public Long2ObjectOpenHashMap<ProfilingInfo> childs = new Long2ObjectOpenHashMap<>();
	public long enterTime = -1;
	public boolean reset = false;
}
