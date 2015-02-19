package com.focusit.agent.metrics.samples;

import java.util.LinkedList;

/**
 * Created by Denis V. Kirpichenkov on 15.02.15.
 */
public class ThreadCallStat {
	public long threadId;
	public int methodId;
	public long count;
	public long totalTime;
	public long exceptions;
	public long minTime;
	public long maxTime;
	public LinkedList<Long> stack = new LinkedList<>();
}
