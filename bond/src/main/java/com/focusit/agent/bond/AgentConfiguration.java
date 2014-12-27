package com.focusit.agent.bond;

import com.focusit.agent.utils.common.StringUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Agent's configuration
 * <p/>
 * Created by Denis V. Kirpichenkov on 06.12.14.
 */
public class AgentConfiguration {
	private static Properties properties = null;
	private static Integer dumpInterval = null;
	private static Integer timerInterval = null;

	private static String excludes[] = null;
	private static String ignoreExcludes[] = null;

	private static String includes[] = null;
	private static String ignoreIncludes[] = null;

	private static int statisticsDumpBatch = -1;
	private static int jvmDumpBatch = -1;

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

		getExcludeClasses();
		getIgnoreExcludeClasses();

		getIncludeClasses();
		getIgnoreIncludeClasses();
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

	public static boolean isClassExcluded(String className)
	{
		boolean skip = false;

		if (excludes != null && excludes.length>0)
		{
			for (String exclude : excludes)
			{
				if (className.startsWith(exclude) || exclude.equalsIgnoreCase("*"))
				{
					skip = true;
					break;
				}
			}

			for (String ignoreExclude : ignoreExcludes)
			{
				if (className.startsWith(ignoreExclude))
				{
					skip = false;
					break;
				}
			}
		}

		if(includes!=null && includes.length>0){
			for (String include : includes)
			{
				if (className.startsWith(include) || include.equalsIgnoreCase("*"))
				{
					skip = false;
					break;
				} else {
					if(!skip)
						skip=true;
				}
			}

			for (String ignoreInclude : ignoreIncludes)
			{
				if (className.startsWith(ignoreInclude))
				{
					skip = true;
					break;
				}
			}
		}

		return skip;
	}

	public static int getThreadJoinTimeout() {
		return 100000;
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
		String result = System.getProperty("methods.file");
		if(result==null || result.trim().length()==0)
			result = "methods.data";
		return result;
	}

	public static String getStatisticsFile() {
		String result = System.getProperty("statistics.file");
		if(result==null || result.trim().length()==0)
			result = "profile.data";
		return result;
	}

	public static String getJvmMonitoringFile() {
		String result = System.getProperty("jvm.file");
		if(result==null || result.trim().length()==0)
			result = "jvm.data";
		return result;
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

		if(excludes!=null)
			return excludes;

		String values = properties.getProperty("agent.exclude");
		if(values==null){
			excludes = new String[0];
			return excludes;
		}
		values = values.trim();
		if (StringUtils.isEmpty(values)) {
			return new String[0];
		}
		excludes = values.split(",");
		return excludes;
	}

	public static int getStatisticsDumpBatch(){
		if(statisticsDumpBatch>0)
			return statisticsDumpBatch;

		int result = 1000;
		try {

			String batch = properties.getProperty("agent.dump.statistics.batch");
			if (!StringUtils.isEmpty(batch)) {
				try {
					result = Integer.parseInt(batch);
				} catch (NumberFormatException e) {
					result = 1000;
				}
			}
		} finally {
			statisticsDumpBatch = result;
			return statisticsDumpBatch;
		}
	}

	public static int getJvmDumpBatch(){
		if(jvmDumpBatch>0)
			return jvmDumpBatch;

		int result = 1000;
		try {

			String batch = properties.getProperty("agent.dump.jvm.batch");
			if (!StringUtils.isEmpty(batch)) {
				try {
					result = Integer.parseInt(batch);
				} catch (NumberFormatException e) {
					result = 1000;
				}
			}
		} finally {
			jvmDumpBatch = result;
			return jvmDumpBatch;
		}
	}
	public static String[] getIgnoreExcludeClasses() {

		if(ignoreExcludes!=null)
			return ignoreExcludes;

		String values = properties.getProperty("agent.exclude.ignore");
		if(values==null){
			ignoreExcludes = new String[0];
			return ignoreExcludes;
		}
		values = values.trim();
		if (StringUtils.isEmpty(values)) {
			return new String[0];
		}
		ignoreExcludes = values.split(",");
		return ignoreExcludes;
	}

	public static String[] getIncludeClasses() {

		if(includes!=null)
			return includes;

		String values = properties.getProperty("agent.include");
		if(values==null){
			includes = new String[0];
			return includes;
		}
		values = values.trim();
		if (StringUtils.isEmpty(values)) {
			return new String[0];
		}
		includes = values.split(",");
		return includes;
	}

	public static String[] getIgnoreIncludeClasses() {

		if(ignoreIncludes!=null)
			return ignoreIncludes;

		String values = properties.getProperty("agent.include.ignore");
		if(values==null){
			ignoreIncludes = new String[0];
			return ignoreIncludes;
		}
		values = values.trim();
		if (StringUtils.isEmpty(values)) {
			return new String[0];
		}
		ignoreIncludes = values.split(",");
		return ignoreIncludes;
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

	public static DumpType getDumpType(){
		String dumpType = properties.getProperty("agent.dumptype");
		if(dumpType!=null && dumpType.trim().length()>0){
			return DumpType.valueOf(dumpType);
		}
		return DumpType.disk;
	}

	public enum Transformer {
		asm, javaassist, cglib
	}

	public static String getNettyDumpHost(){
		return "localhost";
	}

	public static String getNettyDumpJvmPort(){
		return "16000";
	}

	public static String getNettyDumpOSPort(){
		return "16001";
	}

	public static String getNettyDumpMethodMapPort(){
		return "16002";
	}

	public static String getNettyDumpStatisticsPort(){
		return "16003";
	}

	public static String getNettyDumpId(){
		return "app";
	}

	public enum DumpType {netty, disk}

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
		int result = 1000;
		try {

			String batch = properties.getProperty("agent.jvm.monitoring.interval");
			if (!StringUtils.isEmpty(batch)) {
				try {
					result = Integer.parseInt(batch);
				} catch (NumberFormatException e) {
					result = 1000;
				}
			}
		} finally {
			return result;
		}
	}
}
