package com.focusit.agent.analyzer.data.statistics;

/**
 * Created by Denis V. Kirpichenkov on 03.01.15.
 */
public class MethodCallSample {
	public final long threadId;
	public final String name;
	public final long index;
	public final long startTime;
	public final long finishTime;

	public MethodCallSample(long threadId, String name, long index, long startTime, long finishTime) {
		this.threadId = threadId;
		this.name = name;
		this.index = index;
		this.startTime = startTime;
		this.finishTime = finishTime;
	}
}
