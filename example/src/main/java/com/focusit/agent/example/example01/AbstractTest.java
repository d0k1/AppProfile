package com.focusit.agent.example.example01;

/**
 * Created by Denis V. Kirpichenkov on 22.12.14.
 */
public abstract class AbstractTest implements TestInterface {
	public void foo(){
		int test = 0;
		for(int j=0;j<5;){
			j++;
			test+=bar();
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

	public abstract int bar();
}
