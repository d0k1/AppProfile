package com.focusit.utils.metrics.store;

/**
 * Common agent data storage interface
 * Created by Denis V. Kirpichenkov on 09.12.14.
 */
public interface Storage {
	void dumpRest();

	void exit() throws InterruptedException;

	void start();
}
