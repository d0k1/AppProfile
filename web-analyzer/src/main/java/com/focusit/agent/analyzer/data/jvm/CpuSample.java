package com.focusit.agent.analyzer.data.jvm;

/**
 * Created by Denis V. Kirpichenkov on 24.12.14.
 */
public class CpuSample {
	public final double process;
	public final double system;
	public final long time;

	public CpuSample(double process, double system, long time) {
		this.process = process;
		this.system = system;
		this.time = time;
	}
}
