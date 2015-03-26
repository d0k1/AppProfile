package com.focusit.agent.example.example01;

import java.io.IOException;

/**
 * Created by Denis V. Kirpichenkov on 27.11.14.
 */
public class ClassToInstrument extends AbstractTest implements TestInterfaceEnhancerBySpringCGLIB1 {
	public ClassToInstrument(){
		int i = 0;
		i++;
	}

	public int bar(){
		int result = 0;
		for(result=0;result<10000;){
			result++;
		}
		return result;
	}

	public void bar2() throws IOException {
		bar2Int();
	}

	private void bar2Int(){
		for(int i=0;i<10;){
			i++;
//			throw new RuntimeException();
//			throw new IOException();
		}
	}
}
