package com.focusit.agent.example.example01;

/**
 * Created by Denis V. Kirpichenkov on 27.11.14.
 */
public class ClassToInstrument {
	public void foo(){
		for(int j=0;j<1000;){
			j++;
			bar();
		}
	}

	public void bar(){
		for(int i=0;i<10000;){
			i++;
		}
	}
}
