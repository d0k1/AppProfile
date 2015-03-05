package com.focusit.agent.example.profiler;

import com.focusit.agent.metrics.Statistics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Denis V. Kirpichenkov on 03.03.15.
 */
public class HeavyLoadProfilerExample {

	public static Runnable getRunnable(){
		return new Runnable() {
			public final AtomicLong methodId = new AtomicLong(0L);

			void store() throws InterruptedException {
				Statistics.storeEnter(methodId.getAndIncrement());
			}

			void leave() throws InterruptedException {
				Statistics.storeLeave(methodId.getAndDecrement());
			}
			@Override
			public void run() {
				int tries = 100;
				for(int i=0;i<tries;i++){
						try {
							store();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						try {
							leave();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
				}
			}
		};
	}

	public static void main(String[] args) throws InterruptedException {
		int threads = 20;
		Thread t[] = new Thread[threads];
		for(int i=0;i<threads;i++){
			t[i] = new Thread(getRunnable());
		}

		for(int i=0;i<threads;i++) {
			t[i].start();
		}

		for(int i=0;i<threads;i++) {
			t[i].join();
		}
	}
}
