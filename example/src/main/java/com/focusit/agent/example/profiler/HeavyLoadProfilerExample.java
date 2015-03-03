package com.focusit.agent.example.profiler;

import com.focusit.agent.metrics.Statistics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Denis V. Kirpichenkov on 03.03.15.
 */
public class HeavyLoadProfilerExample {
	public final static AtomicLong methodId = new AtomicLong(0L);

	public static Runnable getRunnable(){
		return new Runnable() {
			void store() throws InterruptedException {
				Statistics.storeEnter(methodId.get());
			}

			void leave() throws InterruptedException {
				Statistics.storeLeave(methodId.get());
			}
			@Override
			public void run() {
				for(int i=0;i<3000;i++){
					if(i%2==0){
						try {
							store();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}else {
						try {
							leave();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		};
	}

	public static void main(String[] args) throws InterruptedException {
		Thread t = new Thread(getRunnable());
		t.start();
		t.join();
	}
}
