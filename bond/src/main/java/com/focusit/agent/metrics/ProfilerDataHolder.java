package com.focusit.agent.metrics;

import com.focusit.agent.metrics.samples.ProfilingInfo;
import com.focusit.agent.utils.common.LongObjectRedBlackTree;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Denis V. Kirpichenkov on 19.02.15.
 */
public class ProfilerDataHolder {
	private final static ReentrantLock cleanupLock = new ReentrantLock();

	private final static ProfilerDataHolder instance = new ProfilerDataHolder();
	private final static List<ThreadProfilingControl> controls = new ArrayList<>(1024);

	public static final ProfilerDataHolder getInstance(){
		return instance;
	}

	public static ThreadProfilingControl getThreadControl(){
		cleanupLock.lock();
		try {

			ThreadProfilingControl item = new ThreadProfilingControl();
			controls.add(item);
			return item;

		} finally {
			cleanupLock.unlock();
		}
	}

	private long printMethodStat(ProfilingInfo parent, final ProfilingInfo s, final int level, final StringBuilder builder){
		final AtomicLong totalCount = new AtomicLong(s.count);

	    String currentLine = String.format("\"%d\";\"%d\";\"%s\";\"%d\";\"%s\";\"%d\";\"%d\";\"%d\";\"%d\"\r\n", s.exceptions, s.threadId, parent==null?"null":parent.toString(), level, MethodsMap.getMethod((int) s.methodId), s.count, s.minTime, s.maxTime, s.totalTime);

		if(builder!=null){
			builder.append(currentLine);
		} else {
			System.out.print(currentLine);
		}

		s.childs.forEach(new LongObjectRedBlackTree.IterateFunction<ProfilingInfo>() {
			@Override
			public void process(long key, ProfilingInfo value) {
				totalCount.addAndGet(printMethodStat(s, value, level + 1, builder));
			}
		});

		return totalCount.get();
	}

	public void printData(){
		final AtomicLong totalCount = new AtomicLong(0);

		for(ThreadProfilingControl control:controls) {
			control.roots.forEach(new LongObjectRedBlackTree.IterateFunction<ProfilingInfo>() {
				@Override
				public void process(long key, ProfilingInfo value) {

					totalCount.addAndGet(printMethodStat(null, value, 0, null));
				}
			});
		}
		System.out.println("Total call count "+totalCount.get());
	}

	public String getStringData() throws InterruptedException {
		cleanupLock.lock();

		try {
			final StringBuilder builder = new StringBuilder();

			for (ThreadProfilingControl control : controls) {

				//TODO Add unsafe volatile write
				control.useLock = true;

				control.condition.await();
				control.condition.signal();
				try {
					control.roots.forEach(new LongObjectRedBlackTree.IterateFunction<ProfilingInfo>() {
						@Override
						public void process(long key, ProfilingInfo value) {
							printMethodStat(null, value, 0, builder);
						}
					});
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
