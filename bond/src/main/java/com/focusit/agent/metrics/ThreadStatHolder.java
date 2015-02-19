package com.focusit.agent.metrics;

import com.focusit.agent.metrics.samples.ThreadCallStat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Denis V. Kirpichenkov on 19.02.15.
 */
public class ThreadStatHolder {
	private final static ThreadStatHolder instance = new ThreadStatHolder();
	private final List<ThreadControl> controls = new ArrayList<>();

	public static final ThreadStatHolder getInstance(){
		return instance;
	}

	public ThreadControl getThreadControl(){
		ThreadControl item = new ThreadControl();
		controls.add(item);
		return item;
	}

	public void printData(){
		long totalCount = 0;

		for(ThreadControl control:controls) {
			for (Map.Entry<Long, ThreadCallStat> entry : control.stat.entrySet()) {
				ThreadCallStat s = entry.getValue();
				totalCount += s.count;
				System.out.println(String.format("%d;%s;%d;%d;%d;%d", s.threadId, MethodsMap.getMethod(s.methodId), s.count, s.minTime, s.maxTime, s.totalTime));
			}
		}
		System.out.println("Total call count "+totalCount);
	}
}
