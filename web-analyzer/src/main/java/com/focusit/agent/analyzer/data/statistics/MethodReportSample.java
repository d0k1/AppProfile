package com.focusit.agent.analyzer.data.statistics;

/**
 * Class to hold data on method execution statistics:
 * Call count, Min Time, Max Time, Count of threads called this method,
 * Created by Denis V. Kirpichenkov on 25.01.15.
 */
public class MethodReportSample {
	public long methodId;
	public String methodName;
	public long callCount;
	public long minTime;
	public long maxTime;
}
