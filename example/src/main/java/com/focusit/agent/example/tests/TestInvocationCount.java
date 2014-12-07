package com.focusit.agent.example.tests;

import com.focusit.agent.loader.jassie.AgentLoader;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Class to check stored invocations count and method used in programm
 * <p/>
 * Created by Denis V. Kirpichenkov on 07.12.14.
 */
public class TestInvocationCount {
	static {
		try {
			AgentLoader.loadAgent();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static class TestInvocable {
		private int result = -1;

		public void foo() {
			for (int i = 0; i < 100; i++) {
				bar(i);
			}
		}

		public void bar(int val) {
			int j = 100;
			int x = j * val;
			result = x;
		}
	}

	public static void main(String[] args) {
		TestInvocable test = new TestInvocable();

		test.foo();
	}
}
