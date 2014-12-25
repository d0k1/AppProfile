package com.focusit.agent.analyzer.data.jvm;

/**
 * Created by Denis V. Kirpichenkov on 24.12.14.
 */
public class HeapSample {
	public final long heapInit;
	public final long heapUsed;
	public final long heapCommited;
	public final long heapMax;

	public HeapSample(long heapInit, long heapUsed, long heapCommited, long heapMax) {
		this.heapInit = heapInit;
		this.heapUsed = heapUsed;
		this.heapCommited = heapCommited;
		this.heapMax = heapMax;
	}
}
