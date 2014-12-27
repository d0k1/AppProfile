package com.focusit.agent.mongo;

/**
 * Properties to netty data loaders
 * <p/>
 * Created by Denis V. Kirpichenkov on 14.12.14.
 */
public class MongoConfiguration {

	public static String getHost() {
		return "localhost";
	}

	public static String getPort() {
		return "27017";
	}

	public static String getDbname() {
		return "bond";
	}

	public static String getUser() {
		return null;
	}

	public static String getPassword() {
		return null;
	}

	public static String getStatisticsCollection() {
		return "statistics";
	}

	public static String getMethodsMapCollection() {
		return "methodsmap";
	}

	public static String getJvmMonitoringCollection() {
		return "jvmmonitoring";
	}

	public static String getSessionCollection() {
		return "sessions";
	}

	public static String getMethodMapFile() {
		return "methods.data";
	}

	public static String getStatisticsFile() {
		return "profile.data";
	}

	public static String getJvmMonitoringFile() {
		return "jvm.data";
	}
}
