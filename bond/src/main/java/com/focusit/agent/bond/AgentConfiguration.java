package com.focusit.agent.bond;

import com.focusit.agent.utils.common.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Agent's configuration
 * <p/>
 * Created by Denis V. Kirpichenkov on 06.12.14.
 */
public class AgentConfiguration {
	private static final Logger LOG = Logger.getLogger(AgentConfiguration.class.getName());
	private static Properties properties = null;
	private static Integer dumpInterval = null;
	private static Integer timerInterval = null;

	static {
		Properties p = System.getProperties();
		Enumeration keys = p.keys();
		while (keys.hasMoreElements()) {
			String key = (String)keys.nextElement();
			String value = (String)p.get(key);
			//LOG.finer(key + "='" + value+"'");
		}

		String propertyFile = System.getProperty("agent.config").trim();
		properties = new Properties();
		if (!StringUtils.isEmpty(propertyFile)) {
			try {
				properties.load(new FileInputStream(propertyFile));
			} catch (IOException e) {
			}
		}

		getDumpInterval();
		getTimerPrecision();
	}

	public static int getDumpInterval() {
		if(dumpInterval!=null)
			return dumpInterval;

		int result = 10;
		try {

			String interval = properties.getProperty("agent.dump.interval");
			if (!StringUtils.isEmpty(interval)) {
				try {
					result = Integer.parseInt(interval);
				} catch (NumberFormatException e) {
					result = 10;
				}
			}
		} finally {
			dumpInterval = result;
			return dumpInterval;
		}
	}

	public static int getThreadJoinTimeout() {
		return 10000;
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

	public static String getSessionsFile() {
		return "sessions.data";
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
		if(timerInterval!=null)
			return timerInterval;

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
			timerInterval = result;
			return timerInterval;
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
