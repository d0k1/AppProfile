package com.focusit.agent.metrics.dump;

/**
 * Common agent data storage interface
 * Created by Denis V. Kirpichenkov on 09.12.14.
 */
public interface SamplesDataDumper {
	void dumpRest();

	void exit() throws InterruptedException;

	void start();
}
