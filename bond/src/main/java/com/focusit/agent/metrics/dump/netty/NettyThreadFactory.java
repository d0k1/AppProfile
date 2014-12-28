package com.focusit.agent.metrics.dump.netty;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyThreadFactory implements ThreadFactory {
	private final String name;
	private static final AtomicInteger counter = new AtomicInteger();

	public NettyThreadFactory(String name) {
		this.name = name;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread thread = new Thread(r, name+"-"+ counter.getAndIncrement());
		thread.setDaemon(true);
		return thread;
	}
}
