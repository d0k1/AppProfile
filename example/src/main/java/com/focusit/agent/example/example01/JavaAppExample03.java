package com.focusit.agent.example.example01;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by Denis V. Kirpichenkov on 24.03.15.
 */
public class JavaAppExample03 {

	public static void main(String[] args) throws IOException {

		Runnable work = new Runnable() {
			@Override
			public void run() {
				try {
					new ClassToInstrument().bar2();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};

		ExecutorService executor = Executors.newFixedThreadPool(10);

		for(int i=0;i<1000000;i++){
			executor.submit(work);
		}
		System.in.read();
		executor.shutdown();
	}
}
