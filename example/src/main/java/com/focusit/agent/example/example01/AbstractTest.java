package com.focusit.agent.example.example01;

/**
 * Created by Denis V. Kirpichenkov on 22.12.14.
 */
public abstract class AbstractTest implements TestInterface {
	public void foo(){
		for(int j=0;j<1000;){
			j++;
			bar();
		}
	}

	public abstract void bar();
}
