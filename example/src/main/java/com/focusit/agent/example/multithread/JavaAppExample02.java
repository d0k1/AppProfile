package com.focusit.agent.example.multithread;

import com.focusit.agent.loader.jassie.AgentLoader;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by Denis V. Kirpichenkov on 02.12.14.
 */
public class JavaAppExample02 {
	static {
		try {
			AgentLoader.loadAgent();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws InterruptedException {
		int count = 2;
		Thread threads[] = new Thread[count];

		for(int i=0;i<count;i++){
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					for(int i=0;i<10010;i++) {
						new ClassThree().doMostValuableJob(new ClassTwo());
					}
				}
			});
			threads[i] = t;
			t.start();
		}

		for(int i=0;i<count;i++){
			threads[i].join();
		}
	}
}
