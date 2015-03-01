package com.focusit.agent.bond;

import com.focusit.agent.metrics.JvmMonitoring;
import com.focusit.agent.metrics.OSMonitoring;
import com.focusit.agent.metrics.ProfilerDataHolder;
import com.focusit.agent.metrics.Statistics;
import com.focusit.agent.utils.jmm.FinalBoolean;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Utility class to manage agent: eventId, stop, reset metrics
 * Created by Denis V. Kirpichenkov on 05.12.14.
 */
public class AgentManager {

	private final static ReentrantLock lock = new ReentrantLock(true);

	public static long getAppId(){
		return AgentConfiguration.getAppId();
	}

	public static void stopJvmMonitoring(){
		lock.lock();
		try{
			JvmMonitoring.enabled = new FinalBoolean(false);
		} finally {
			lock.unlock();
		}
	}

	public static void startJvmMonitoring(){
		lock.lock();
		try{
			JvmMonitoring.enabled = new FinalBoolean(true);
		} finally {
			lock.unlock();
		}
	}

	public static void stopOsMonitoring(){
		lock.lock();
		try{
			OSMonitoring.enabled = new FinalBoolean(false);
		} finally {
			lock.unlock();
		}
	}

	public static void startOsMonitoring(){
		lock.lock();
		try{
			OSMonitoring.enabled = new FinalBoolean(true);
		} finally {
			lock.unlock();
		}
	}

	public static void stopProfiling(){
		lock.lock();
		try{
			Statistics.enabled = new FinalBoolean(false);
		} finally {
			lock.unlock();
		}
	}

	public static void startProfiling(){
		lock.lock();
		try{
			Statistics.enabled = new FinalBoolean(true);
		} finally {
			lock.unlock();
		}
	}

	public static void cleanUpProfiler(){
		lock.lock();
		try{
			ProfilerDataHolder.getInstance().cleanUp();
		} catch (InterruptedException e) {
			System.err.println("CleanUpProfiler error: " + e.toString());
		} finally {
			lock.unlock();
		}
	}

	public static String getProfilerData() throws InterruptedException {
		return ProfilerDataHolder.getInstance().getStringData();
	}
}
