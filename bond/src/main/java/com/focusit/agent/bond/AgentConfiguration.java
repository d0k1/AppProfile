package com.focusit.agent.bond;

import org.apache.commons.lang3.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
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

	public static String getMongoDbHost() {
		return "127.0.0.1";
	}

	public static String getMongoDbPort() {
		return "28017";
	}

	public static String getMongoDbInitialDb() {
		return "bond";
	}

	public static boolean getMongoDbCreateDbOnStart() {
		return true;
	}

	public static String getMethodsMapFile() {
		return "methods.data";
	}

	public static String getStatisticsFile() {
		return "profile.data";
	}

	public static String getJvmMonitoringFile() {
		return "jvm.data";
	}

	public static boolean isAgentEnabled() {
		String enabledValue = properties.getProperty("agent.enabled");
		return !(enabledValue == null || !enabledValue.equalsIgnoreCase(Boolean.toString(true)));

	}

	public static Transformer getAgentClassTransformer() {
		String transformer = properties.getProperty("agent.transformer");
		return Transformer.valueOf(transformer);
	}

	public static String[] getExcludeClasses() {
		String excludes = properties.getProperty("agent.exclude");
		excludes = excludes.trim();
		if (StringUtils.isEmpty(excludes)) {
			return new String[0];
		}
		return excludes.split(",");
	}

	public static String[] getIgnoreExcludeClasses() {
		String igonres = properties.getProperty("agent.exclude.ingore");
		igonres = igonres.trim();
		if (StringUtils.isEmpty(igonres)) {
			return new String[0];
		}
		return igonres.split(",");
	}

	public static int getTimerPrecision() {
		int result = 10;
		try {
			String interval = properties.getProperty("agent.timer.interval");
			if (!StringUtils.isEmpty(interval)) {
				try {
					result = Integer.parseInt(interval);
				} catch (NumberFormatException e) {
					result = 10;
				}
			}
		} finally {
			return result;
		}
	}

	public enum Transformer {
		asm, javaassist, cglib
	}

	public static URL getAgentLog4jProps() {
		return ClassLoader.getSystemClassLoader().getResource("log4j.properties");
	}

	public enum StorageType {
		disk, mongodb, netty
	}

	public static StorageType getActiveStorgeType() {
		return StorageType.disk;
	}

	public static int getJvmMonitoringInterval() {
		return 2000;
	}
}
