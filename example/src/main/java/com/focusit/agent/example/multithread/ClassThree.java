package com.focusit.agent.example.multithread;

/**
 * Created by Denis V. Kirpichenkov on 02.12.14.
 */
public class ClassThree {
	public void doMostValuableJob(ClassTwo two){
		two.doLessValuableJob(new ClassOne());
	}
}
