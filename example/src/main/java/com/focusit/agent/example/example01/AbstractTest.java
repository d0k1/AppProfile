package com.focusit.agent.example.example01;

/**
 * Created by Denis V. Kirpichenkov on 22.12.14.
 */
public abstract class AbstractTest implements TestInterface {
	public void foo(){
		for(int j=0;j<100;){
			j++;
			bar();
		}
	}

//	public void finallyTest(){
//		try{
//			int i=0;
//			i++;
//		} finally {
//			Statistics.
//		}
//	}

	public abstract void bar();
}
