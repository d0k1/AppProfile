package com.focusit.agent.example.example01;

import com.focusit.agent.loader.jassie.AgentLoader;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by Denis V. Kirpichenkov on 28.12.14.
 */
public class LongRunExample {
	static {
		try {
			AgentLoader.loadAgent(JavaAppExample01.class.getClassLoader());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void main(String[] args) throws InterruptedException {
		System.err.println("Begin!");

		Thread thread = new Thread(new Runnable() {
			private int i = 0;
			@Override
			public void run() {
				while(!Thread.interrupted()) {
					if (i++ >= 15)
						break;

					new ClassToInstrument().foo();
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		Thread thread2 = new Thread(new Runnable() {
			private int i = 0;
			@Override
			public void run() {
				while(!Thread.interrupted()) {
					if (i++ >= 5)
						break;

					new ClassToInstrument().foo();
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		thread2.start();
		thread.start();

		thread.join();
		System.err.println("Done!");
	}
}
