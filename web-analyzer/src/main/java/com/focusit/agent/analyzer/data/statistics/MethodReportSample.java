package com.focusit.agent.analyzer.data.statistics;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Class to hold data on method execution statistics:
 * Call count, Min Time, Max Time, Count of threads called this method,
 * Created by Denis V. Kirpichenkov on 25.01.15.
 */
public class MethodReportSample {
	public long appId;
	public long sessionId;
	public long recId;
	public long methodId;
	public String methodName;
	private long callCount=0;
	private long minTime=Long.MAX_VALUE;
	private long maxTime=Long.MIN_VALUE;
	private long totalTime = 0;

	private ReentrantLock lock = new ReentrantLock(true);

	public MethodReportSample(long appId, long sessionId, long recId, long methodId, String methodName) {
		this.appId = appId;
		this.sessionId = sessionId;
		this.recId = recId;
		this.methodId = methodId;
		this.methodName = methodName;
	}

	public MethodReportSample(long appId, long sessionId, long recId, long methodId, String methodName, long callCount, long minTime, long maxTime, long totalTime) {
		this.appId = appId;
		this.sessionId = sessionId;
		this.recId = recId;
		this.methodId = methodId;
		this.methodName = methodName;
		this.callCount = callCount;
		this.minTime = minTime;
		this.maxTime = maxTime;
		this.totalTime = totalTime;
	}

	public void addCallCount(long time){
		lock.lock();
		try{
			callCount++;
			totalTime+=time;

			if(minTime>time){
				minTime = time;
			}

			if(maxTime<time){
				maxTime = time;
			}

		} finally {
			lock.unlock();
		}
	}

	public long getTotalTime() {
		return totalTime;
	}

	public long getCallCount() {
		return callCount;
	}

	public long getMinTime() {
		return minTime;
	}

	public long getMaxTime() {
		return maxTime;
	}
}
