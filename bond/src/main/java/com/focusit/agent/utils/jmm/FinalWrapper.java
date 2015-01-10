package com.focusit.agent.utils.jmm;

/**
 * Class to correct work with fields in multithreaded environment
 * Created by Denis V. Kirpichenkov on 10.01.15.
 */
public class FinalWrapper<T> {
	public final T value;

	public FinalWrapper(T value) {
		this.value = value;
	}
}
