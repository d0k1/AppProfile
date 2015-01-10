package com.focusit.agent.bond.time;

import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.sleep;
import static java.lang.Thread.yield;

/**
 * Thread providing eventId. Used to avoid multiple getting system eventId
 * Created by Denis V. Kirpichenkov on 25.11.14.
 */
public class GlobalTime {
	private static final AtomicLong time = new AtomicLong(0L);
	private static final AtomicLong timestamp = new AtomicLong(0L);
	private final int interval;

	public GlobalTime(int interval) {
		this.interval = interval;
		time.set(System.nanoTime());
		timestamp.set(System.currentTimeMillis());
	}

	public static long getCurrentTime() {
		return time.get();
	}

	public static long getCurrentTimeInMillis() {
		return timestamp.get();
	}

	public void start() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!Thread.interrupted()) {

					time.set(System.nanoTime());
					timestamp.set(System.currentTimeMillis());
					try {
						if(interval>0) {
							sleep(interval);
						} else {
							yield();
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}, "agent global time");
		thread.setDaemon(true);
		thread.start();
	}
}
