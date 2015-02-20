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

	private long printMethodStat(ThreadCallStat s, int level){
		long totalCount = s.count;

		for(int i=0;i<level;i++){
			System.out.print("-");
		}
		System.out.println(String.format("%d;%s;%d;%d;%d;%d", s.threadId, MethodsMap.getMethod(s.methodId), s.count, s.minTime, s.maxTime, s.totalTime));

		for(Map.Entry<Long, ThreadCallStat> entry : s.childs.entrySet()){
			totalCount += printMethodStat(entry.getValue(), level+1);
		}

		return totalCount;
	}

	public void printData(){
		long totalCount = 0;

		for(ThreadControl control:controls) {
			for (Map.Entry<Long, ThreadCallStat> entry : control.roots.entrySet()) {
				ThreadCallStat s = entry.getValue();
				totalCount += printMethodStat(s, 0);
			}
		}
		System.out.println("Total call count "+totalCount);
	}
}
