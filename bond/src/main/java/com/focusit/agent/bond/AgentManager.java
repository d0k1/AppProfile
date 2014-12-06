package com.focusit.agent.bond;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.jar.JarFile;

/**
 * Utility class to manage agent: start, stop, reset metrics
 * Created by Denis V. Kirpichenkov on 05.12.14.
 */
public class AgentManager {

	public static JarFile agentJar = null;

	public static void addShutodwnHook() {

		try {
			new RandomAccessFile(AgentConfiguration.getMethodsMapFile(), "rw").close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.err.println("Manager: " + ClassLoader.getSystemClassLoader());
	}

	public void setAgentJar(String jarPath) throws IOException {
		agentJar = new JarFile(jarPath);
	}
}
