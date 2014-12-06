package com.focusit.agent.bond;

import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Agent's configuration
 * <p/>
 * Created by Denis V. Kirpichenkov on 06.12.14.
 */
public class AgentConfiguration {
	private static Properties properties = null;

	static {
		String propertyFile = System.getProperty("agent.config").trim();
		properties = new Properties();
		if (!StringUtils.isEmpty(propertyFile)) {
			try {
				properties.load(new FileInputStream(propertyFile));
			} catch (IOException e) {
			}
		}
	}

	public static String getMethodsMapFile() {
		return "methods.data";
	}

	public static String getProfieFile() {
		return "profile.data";
	}

	public static boolean isAgentEnabled() {
		String enabledValue = properties.getProperty("agent.enabled");
		if (enabledValue == null || !enabledValue.equalsIgnoreCase(Boolean.toString(true))) {
			return false;
		}

		return true;
	}

	public static Transformer getAgentClassTransformer() {
		String transformer = properties.getProperty("agent.transformer");
		return Transformer.valueOf(transformer);
	}

	public static String[] getExcludeClasses() {
		String excludes = properties.getProperty("agent.exclude");
		excludes = excludes.trim();
		if (StringUtils.isEmpty(excludes)) {
			String result[] = new String[0];
			return result;
		}
		return excludes.split(",");
	}

	public static String[] getIgnoreExcludeClasses() {
		String igonres = properties.getProperty("agent.exclude.ingore");
		igonres = igonres.trim();
		if (StringUtils.isEmpty(igonres)) {
			String result[] = new String[0];
			return result;
		}
		return igonres.split(",");
	}

	public enum Transformer {
		asm, javaassist, cglib
	}
}
