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

	private static String excludes[] = null;
	private static String ignoreExcludes[] = null;

	private static String includes[] = null;
	private static String ignoreIncludes[] = null;

	private static int statisticsDumpBatch = -1;
	private static int dumpBatch = -1;
	private static long appId = -1L;

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

		getExcludeClasses();
		getIgnoreExcludeClasses();

		getIncludeClasses();
		getIgnoreIncludeClasses();

		getAppId();
	}

	public static int getNettySessionPort(){
		int result = 15999;
		try {

			String port = properties.getProperty("agent.netty.session.port");
			if (!StringUtils.isEmpty(port)) {
				try {
					result = Integer.parseInt(port);
				} catch (NumberFormatException e) {
					result = 15999;
				}
			}
		} finally {
			return result;
		}
	};

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

	private static boolean matchMask(String className, String mask){
		if(mask.length()<=3 || mask.indexOf(".*")!=0)
			return false;

		return className.matches(mask);
	}
	public static boolean isClassExcluded(String className)
	{
		boolean skip = false;

		if (excludes != null && excludes.length>0)
		{
			for (String exclude : excludes)
			{
				if (className.startsWith(exclude) || exclude.equalsIgnoreCase("*") || matchMask(className, exclude))
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
				if (className.startsWith(include) || include.equalsIgnoreCase("*") || matchMask(className, include))
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
		if(dumpBatch >0)
			return dumpBatch;

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
			dumpBatch = result;
			return dumpBatch;
		}
	}

	public static int getOsDumpBatch(){
		if(dumpBatch>0)
			return dumpBatch;

		int result = 1000;
		try {

			String batch = properties.getProperty("agent.dump.os.batch");
			if (!StringUtils.isEmpty(batch)) {
				try {
					result = Integer.parseInt(batch);
				} catch (NumberFormatException e) {
					result = 1000;
				}
			}
		} finally {
			dumpBatch = result;
			return dumpBatch;
		}
	}

	public static String[] getNetworkInterfaces() {
		String empty[] = new String[0];
		String ifs = properties.getProperty("agent.os.ifaces");

		if(ifs==null) {
			return empty;
		}

		ifs = ifs.trim().toLowerCase();
		String faces[] = ifs.split(",");
		for(String face:faces){
			face = face.trim();
		}

		return faces;
	}

	public static String[] getHdDrives() {
		String empty[] = new String[0];
		String hdds = properties.getProperty("agent.os.hdds");

		if(hdds==null) {
			return empty;
		}

		hdds = hdds.trim().toLowerCase();
		String drives[] = hdds.split(",");
		for(String drive :drives){
			drive = drive.trim();
		}

		return drives;
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

	public static boolean isJvmMonitoringEnabled(){
		String values = properties.getProperty("agent.jvm.enabled");
		if(values==null){
			return true;
		}
		values = values.trim();
		return Boolean.parseBoolean(values);
	}

	public static boolean isOsMonitoringEnabled(){
		String values = properties.getProperty("agent.os.enabled");
		if(values==null){
			return true;
		}
		values = values.trim();
		return Boolean.parseBoolean(values);
	}

	public static boolean isStatisticsEnabled(){
		String values = properties.getProperty("agent.statistics.enabled");
		if(values==null){
			return true;
		}
		values = values.trim();
		return Boolean.parseBoolean(values);
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
		String result = "localhost";
		try {

			String host = properties.getProperty("agent.netty.server.host");
			if (!StringUtils.isEmpty(host)) {
				result = host;
			}
		} finally {
			return result;
		}
	}

	public static int getNettyDumpJvmPort(){
		int result = 16000;
		try {

			String port = properties.getProperty("agent.netty.jvm.port");
			if (!StringUtils.isEmpty(port)) {
				try {
					result = Integer.parseInt(port);
				} catch (NumberFormatException e) {
					result = 16000;
				}
			}
		} finally {
			return result;
		}
	}

	public static int getNettyDumpOSPort(){
		int result = 16001;
		try {

			String port = properties.getProperty("agent.netty.os.port");
			if (!StringUtils.isEmpty(port)) {
				try {
					result = Integer.parseInt(port);
				} catch (NumberFormatException e) {
					result = 16001;
				}
			}
		} finally {
			return result;
		}
	}

	public static int getNettyDumpMethodMapPort(){
		int result = 16002;
		try {

			String port = properties.getProperty("agent.netty.methodsmap.port");
			if (!StringUtils.isEmpty(port)) {
				try {
					result = Integer.parseInt(port);
				} catch (NumberFormatException e) {
					result = 16002;
				}
			}
		} finally {
			return result;
		}
	}

	public static int getNettyDumpStatisticsPort(){
		int result = 16003;
		try {

			String port = properties.getProperty("agent.netty.statistics.port");
			if (!StringUtils.isEmpty(port)) {
				try {
					result = Integer.parseInt(port);
				} catch (NumberFormatException e) {
					result = 16003;
				}
			}
		} finally {
			return result;
		}
	}

	public static int getNettyConnectingInterval(){
		int result = 5000;
		try {

			String interval = properties.getProperty("agent.netty.connection.interval");
			if (!StringUtils.isEmpty(interval)) {
				try {
					result = Integer.parseInt(interval);
				} catch (NumberFormatException e) {
					result = 5000;
				}
			}
		} finally {
			return result;
		}
	}

	public static long getAppId(){
		if(appId>0)
			return appId;

		long result = 1L;
		try {
			String id = properties.getProperty("agent.appId");
			if (!StringUtils.isEmpty(id)) {
				try {
					result = Integer.parseInt(id);
				} catch (NumberFormatException e) {
					result = 1L;
				}
			}
		} finally {
			appId = result;
			return result;
		}
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

	public static int getOsMonitoringInterval() {
		int result = 1000;
		try {

			String batch = properties.getProperty("agent.os.monitoring.interval");
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

	public static int getStatisticsBufferLength(){
		int result = 6553600;
		try {

			String buffer = properties.getProperty("agent.statistics.buffer");
			if (!StringUtils.isEmpty(buffer)) {
				try {
					result = Integer.parseInt(buffer);
				} catch (NumberFormatException e) {
					result = 6553600;
				}
			}
		} finally {
			return result;
		}
	}

	public static int getJvmBufferLength(){
		int result = 655360;
		try {

			String buffer = properties.getProperty("agent.jvm.buffer");
			if (!StringUtils.isEmpty(buffer)) {
				try {
					result = Integer.parseInt(buffer);
				} catch (NumberFormatException e) {
					result = 655360;
				}
			}
		} finally {
			return result;
		}
	}

	public static int getOsBufferLength(){
		int result = 655360;
		try {

			String buffer = properties.getProperty("agent.os.buffer");
			if (!StringUtils.isEmpty(buffer)) {
				try {
					result = Integer.parseInt(buffer);
				} catch (NumberFormatException e) {
					result = 655360;
				}
			}
		} finally {
			return result;
		}
	}
}
