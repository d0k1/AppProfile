package com.focusit.agent.bond;

import com.focusit.agent.metrics.JvmMonitoring;
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
		try{
			lock.lock();
			JvmMonitoring.enabled = new FinalBoolean(false);
		} finally {
			lock.unlock();
		}
	}

	public static void startJvmMonitoring(){
		try{
			lock.lock();
			JvmMonitoring.enabled = new FinalBoolean(true);
		} finally {
			lock.unlock();
		}
	}

	public static void stopStatistics(){
		try{
			lock.lock();
			Statistics.enabled = new FinalBoolean(false);
		} finally {
			lock.unlock();
		}
	}

	public static void startStatistics(){
		try{
			lock.lock();
			Statistics.enabled = new FinalBoolean(true);
		} finally {
			lock.unlock();
		}
	}
}
