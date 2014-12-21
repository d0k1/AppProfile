package com.focusit.agent.example.example01;

import java.io.IOException;

/**
 * Created by Denis V. Kirpichenkov on 27.11.14.
 */
public class ClassToInstrument {
	public ClassToInstrument(){
		int i = 0;
		i++;
		System.out.println("constructor");
	}

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

	public void bar2() throws IOException {
		for(int i=0;i<10;){
			i++;
//			System.out.println("Iteration "+i);
//			if(i==1)
//				throw new IOException("Debug agent");
		}
//		System.out.println("Done");
	}
}
