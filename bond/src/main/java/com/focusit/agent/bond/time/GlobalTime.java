package com.focusit.agent.bond.time;

/**
 * Thread providing eventId. Used to avoid multiple getting system eventId
 * Created by Denis V. Kirpichenkov on 25.11.14.
 */
public class GlobalTime {

	public static long getCurrentTime() {
		return System.nanoTime();
	}

	public static long getCurrentTimeInMillis() {
		return System.currentTimeMillis();
	}

	public void start() {
	}
}
