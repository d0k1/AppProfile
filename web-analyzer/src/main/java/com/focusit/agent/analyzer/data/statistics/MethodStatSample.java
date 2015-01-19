package com.focusit.agent.analyzer.data.statistics;

import java.util.ArrayList;
import java.util.List;

/**
 * Class to hold infomation about method: execution count, threads
 * Created by Denis V. Kirpichenkov on 18.01.15.
 */
public class MethodStatSample {
	public final String method;
	public final List<Long> threads = new ArrayList<>();
	public final List<Long> times = new ArrayList<>();
	public final long callCount;
	public final long minTime;
	public final long maxTime;
	public final long totalTime;

	public MethodStatSample(String method, long callCount, long minTime, long maxTime, long totalTime) {
		this.method = method;
		this.callCount = callCount;
		this.minTime = minTime;
		this.maxTime = maxTime;
		this.totalTime = totalTime;
	}
}
