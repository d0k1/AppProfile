package com.focusit.agent.example.example01;

import com.focusit.agent.loader.jassie.AgentLoader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

/**
 * Example of using agent
 * Created by Denis V. Kirpichenkov on 07.08.14.
 */
public class JavaAppExample01 {
	static {
		try {
			AgentLoader.loadAgent();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws SQLException, IOException, URISyntaxException {
		try {
			new ClassToInstrument().foo();
		} catch (Throwable e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}
