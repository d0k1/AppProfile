package com.focusit.agent.example.example01;

import java.io.IOException;

/**
 * Created by Denis V. Kirpichenkov on 27.11.14.
 */
public class ClassToInstrument extends AbstractTest implements TestInterface {
	public ClassToInstrument(){
		int i = 0;
		i++;
		System.out.println("constructor");
	}

	public void bar(){
		for(int i=0;i<10000;){
			i++;
		}
	}

	public void bar2() throws IOException {
		for(int i=0;i<10;){
			i++;
//			throw new RuntimeException();
//			throw new IOException();
		}
	}
}
