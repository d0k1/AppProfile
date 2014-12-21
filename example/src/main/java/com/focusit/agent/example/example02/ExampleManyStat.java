package com.focusit.agent.example.example02;

import com.focusit.agent.example.example01.ClassToInstrument;
import com.focusit.agent.loader.jassie.AgentLoader;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by Denis V. Kirpichenkov on 19.12.14.
 */
public class ExampleManyStat {
	static {
		try {
			AgentLoader.loadAgent(ExampleManyStat.class.getClassLoader());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void main(String[] args) throws IOException {
		ClassToInstrument cls = new ClassToInstrument();
		for(long i=0;i<42000000;i++){
			cls.bar2();

			if(i%1000000==0)
				System.out.println("Done: " + i + " of 42000000");

			//Thread.yield();
		}
	}
}
