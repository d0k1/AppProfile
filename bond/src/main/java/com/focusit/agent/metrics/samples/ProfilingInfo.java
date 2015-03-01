package com.focusit.agent.metrics.samples;

import com.focusit.agent.utils.common.BondLongObjectMap;

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
	public BondLongObjectMap<ProfilingInfo> childs = new BondLongObjectMap<>(1000);
	public long enterTime = -1;
	public boolean reset = false;
}
