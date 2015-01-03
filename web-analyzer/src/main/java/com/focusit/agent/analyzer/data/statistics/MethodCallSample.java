package com.focusit.agent.analyzer.data.statistics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Denis V. Kirpichenkov on 03.01.15.
 */
public class MethodCallSample {
	public final String _id;
	public final long threadId;
	public final long methodId;
	public final String methodName;
	public long starttimestamp = -1;
	public long finishtimestamp = -1;
	public long startTime = -1;
	public long finishTime = -1;
	public final List<String> parents = new ArrayList<>();

	public MethodCallSample(String id, long threadId, long methodId, String methodName) {
		_id = id;
		this.threadId = threadId;
		this.methodId = methodId;
		this.methodName = methodName;
	}
}
