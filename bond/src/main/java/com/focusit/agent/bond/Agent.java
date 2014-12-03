package com.focusit.agent.bond;

import com.focusit.agent.bond.metrics.MethodsMapDumper;
import com.focusit.agent.bond.metrics.StatisticDumper;
import com.focusit.agent.bond.time.GlobalTime;
import org.apache.commons.lang.StringUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Properties;

/**
 * Agent main class stub
 *
 * Created by Denis V. Kirpichenkov on 06.08.14.
 */
public class Agent {
	private static final String AGENT_ENABLED = "agent.enabled";
	private static final String AGENT_CONFIG = "agent.config";
	private static final String AGENT_TRANSFORMATOR="agent.transformer";
	private static final String AGENT_ASM="asm";
	private static final String AGENT_JAVAASSIT="javaassist";
	private static Instrumentation agentInstrumentation = null;

	public static void premain(String agentArguments, Instrumentation instrumentation) throws FileNotFoundException {
		agentmain(agentArguments, instrumentation);
	}

	public static void agentmain(String agentArguments, Instrumentation instrumentation) throws FileNotFoundException {
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

		GlobalTime gt = new GlobalTime(10);
		gt.start();

		final StatisticDumper statDump = new StatisticDumper("profile.data");
		statDump.start();

		final MethodsMapDumper methodDump = new MethodsMapDumper("methods.data");
		methodDump.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				statDump.doDump();
				try {
					methodDump.doDump();
				} catch (IOException e) {
					e.printStackTrace();
				}

				try {
					statDump.exit();
					methodDump.exit();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		String transformer = properties.getProperty(AGENT_TRANSFORMATOR);

		if(transformer==null)
			transformer = AGENT_JAVAASSIT;
		else
			transformer = transformer.trim();

		agentInstrumentation = instrumentation;
		switch (transformer){
			case AGENT_ASM:
				agentInstrumentation.addTransformer(new AsmClassTransformer(properties));
				break;
			case AGENT_JAVAASSIT:
				agentInstrumentation.addTransformer(new JavaAssistClassTransformer(properties));
				break;
		}
	}
}
