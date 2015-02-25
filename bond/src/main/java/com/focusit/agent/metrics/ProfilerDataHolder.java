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

	private long printMethodStat(ProfilingInfo parent, ProfilingInfo s, int level, StringBuilder builder){
		long totalCount = s.count;

	    String currentLine = String.format("\"%d\";\"%d\";\"%s\";\"%d\";\"%s\";\"%d\";\"%d\";\"%d\";\"%d\"\r\n", s.exceptions, s.threadId, parent==null?"null":parent.toString(), level, MethodsMap.getMethod(s.methodId), s.count, s.minTime, s.maxTime, s.totalTime);

		if(builder!=null){
			builder.append(currentLine);
		} else {
			System.out.print(currentLine);
		}

		for(Map.Entry<Long, ProfilingInfo> entry : s.childs.entrySet()){
			totalCount += printMethodStat(s, entry.getValue(), level+1, builder);
		}

		return totalCount;
	}

	public void printData(){
		long totalCount = 0;

		for(ThreadProfilingControl control:controls) {
			for (Map.Entry<Long, ProfilingInfo> entry : control.roots.entrySet()) {
				ProfilingInfo s = entry.getValue();
				totalCount += printMethodStat(null, s, 0, null);
			}
		}
		System.out.println("Total call count "+totalCount);
	}

	public String getStringData(){
		cleanupLock.lock();

		try {
			StringBuilder builder = new StringBuilder();

			for (ThreadProfilingControl control : controls) {
				control.lock.lock();
				try {
					for (Map.Entry<Long, ProfilingInfo> entry : control.roots.entrySet()) {
						ProfilingInfo s = entry.getValue();
						printMethodStat(null, s, 0, builder);
					}
				} finally {
					control.lock.unlock();
				}
			}

			return builder.toString();
		} finally {
			cleanupLock.unlock();
		}
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
