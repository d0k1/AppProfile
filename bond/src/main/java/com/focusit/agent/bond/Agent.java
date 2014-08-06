package com.focusit.agent.bond;

import org.apache.commons.lang.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Properties;

/**
 * Agent main class stub
 *
 * Created by Denis V. Kirpichenkov on 06.08.14.
 */
public class Agent {
	public static final String AGENT_ENABLED = "agent.enabled";
	public static final String AGENT_CONFIG = "agent.config";
	private static Instrumentation agentInstrumentation = null;

	public static void premain(String agentArguments, Instrumentation instrumentation) {
		agentmain(agentArguments, instrumentation);
	}

	public static void agentmain(String agentArguments, Instrumentation instrumentation) {
		String propertyFile = System.getProperty(AGENT_CONFIG);
		if(StringUtils.isEmpty(propertyFile))
			return;

		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(propertyFile));
		} catch (IOException e) {
			return;
		}

		String enabledValue = properties.getProperty(AGENT_ENABLED);
		if(enabledValue==null || !enabledValue.equalsIgnoreCase(Boolean.toString(true))){
			return;
		}

		properties.remove(AGENT_ENABLED);

		agentInstrumentation = instrumentation;
		agentInstrumentation.addTransformer(new ClassTransformer(properties));
	}
}
