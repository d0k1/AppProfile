package com.focusit.agent.metrics;

import com.focusit.agent.metrics.samples.ProfilingInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Denis V. Kirpichenkov on 19.02.15.
 */
public class ProfilerDataHolder {
	private final static ReentrantLock cleanupLock = new ReentrantLock();

	private final static ProfilerDataHolder instance = new ProfilerDataHolder();
	private final List<ThreadProfilingControl> controls = new ArrayList<>();

	public static final ProfilerDataHolder getInstance(){
		return instance;
	}

	public ThreadProfilingControl getThreadControl(){
		cleanupLock.lock();
		try {

			ThreadProfilingControl item = new ThreadProfilingControl();
			controls.add(item);
			return item;

		} finally {
			cleanupLock.unlock();
		}
	}

	private long printMethodStat(ProfilingInfo s, int level){
		long totalCount = s.count;

		System.out.println(String.format("%d;%d;%d;%s;%d;%d;%d;%d", s.exceptions, level, s.threadId, MethodsMap.getMethod(s.methodId), s.count, s.minTime, s.maxTime, s.totalTime));

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

	public void cleanUp() throws InterruptedException {
		cleanupLock.lockInterruptibly();

		try{

			for(ThreadProfilingControl ctrl:controls){
				ctrl.lock.lockInterruptibly();

				try{
					ctrl.current = null;
					ctrl.stack.clear();
					ctrl.roots.clear();
				} finally {
					ctrl.lock.unlock();
				}
			}
		} finally {
			cleanupLock.unlock();
		}
	}
}
