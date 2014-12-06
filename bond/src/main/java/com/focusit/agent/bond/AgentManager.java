package com.focusit.agent.bond;

import java.io.IOException;
import java.util.jar.JarFile;

/**
 * Utility class to manage agent: start, stop, reset metrics
 * Created by Denis V. Kirpichenkov on 05.12.14.
 */
public class AgentManager {

	public static JarFile agentJar = null;

	public void setAgentJar(String jarPath) throws IOException {
		agentJar = new JarFile(jarPath);
	}
}
