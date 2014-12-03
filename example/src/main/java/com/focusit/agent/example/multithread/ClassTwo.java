package com.focusit.agent.example.multithread;

/**
 * Created by Denis V. Kirpichenkov on 02.12.14.
 */
public class ClassTwo {
	public void doLessValuableJob(ClassOne one){
		for(int i=0;i<10;i++) {
			one.doSomeWhiteNoise();
		}
	}
}
