package com.focusit.agent.bond.time;

import java.util.concurrent.atomic.AtomicLong;

import static java.lang.Thread.sleep;

/**
 * Thread providing time. Used to avoid multiple getting system time
 * Created by Denis V. Kirpichenkov on 25.11.14.
 */
public class GlobalTime {
	private static final AtomicLong time = new AtomicLong(0L);
	private final int interval;

	public GlobalTime(int interval) {
		this.interval = interval;
	}

	public static long getCurrentTime() {
		return time.get();
	}

	public void start() {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!Thread.interrupted()) {
					long temp_time = time.get();
					time.compareAndSet(temp_time, System.nanoTime());
					try {
						sleep(interval);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		thread.setDaemon(true);
		thread.start();
	}
}
