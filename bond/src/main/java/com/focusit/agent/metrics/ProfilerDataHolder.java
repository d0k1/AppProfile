package com.focusit.agent.metrics;

import com.focusit.agent.metrics.samples.ProfilingInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Denis V. Kirpichenkov on 19.02.15.
 */
public class ProfilerDataHolder {
	private final static ProfilerDataHolder instance = new ProfilerDataHolder();
	private final List<ThreadProfilingControl> controls = new ArrayList<>();

	public static final ProfilerDataHolder getInstance(){
		return instance;
	}

	public ThreadProfilingControl getThreadControl(){
		ThreadProfilingControl item = new ThreadProfilingControl();
		controls.add(item);
		return item;
	}

	private long printMethodStat(ProfilingInfo s, int level){
		long totalCount = s.count;

		System.out.print(s.exceptions);
		for(int i=0;i<level;i++){
			System.out.print("-");
		}
		System.out.println(String.format("%d;%s;%d;%d;%d;%d", s.threadId, MethodsMap.getMethod(s.methodId), s.count, s.minTime, s.maxTime, s.totalTime));

		for(Map.Entry<Long, ProfilingInfo> entry : s.childs.entrySet()){
			totalCount += printMethodStat(entry.getValue(), level+1);
		}

		return totalCount;
	}

	public void printData(){
		long totalCount = 0;

		for(ThreadProfilingControl control:controls) {
			for (Map.Entry<Long, ProfilingInfo> entry : control.roots.entrySet()) {
				ProfilingInfo s = entry.getValue();
				totalCount += printMethodStat(s, 0);
			}
		}
		System.out.println("Total call count "+totalCount);
	}
}
