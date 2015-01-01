package com.focusit.agent.analyzer.data.jvm;

/**
 * Created by Denis V. Kirpichenkov on 24.12.14.
 */
public class CpuSample {
	public final double process;
	public final double system;
	public final long timestamp;

	public CpuSample(double process, double system, long timestamp) {
		this.process = process;
		this.system = system;
		this.timestamp = timestamp;
	}
}
