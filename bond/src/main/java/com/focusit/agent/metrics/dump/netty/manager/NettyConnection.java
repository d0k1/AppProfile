package com.focusit.agent.metrics.dump.netty.manager;

import com.focusit.agent.metrics.dump.netty.NettyThreadFactory;
import com.focusit.agent.utils.jmm.FinalBoolean;
import com.focusit.agent.utils.jmm.FinalWrapper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Handles netty connection to specific port
 * Created by Denis V. Kirpichenkov on 10.01.15.
 */
public class NettyConnection {
	private final String host;
	private final int port;
	private final int checkPeriod;

	private FinalBoolean channelConnected = new FinalBoolean(false);
	private FinalWrapper<Bootstrap> bootstrap = null;
	private FinalWrapper<ChannelFuture> future = null;
	private final NettyThreadFactory threadFactory;
	private final ReentrantLock lock = new ReentrantLock();

	public NettyConnection(String host, int port, int checkPeriod, NettyThreadFactory threadFactory) {
		this.host = host;
		this.port = port;
		this.checkPeriod = checkPeriod;
		this.threadFactory = threadFactory;
	}

	public void init(Bootstrap b){
		bootstrap = new FinalWrapper<>(b);

		try {
			tryConnect();
		} catch (Exception e){

		}

		threadFactory.newThread(new Runnable() {
			@Override
			public void run() {
				while (!Thread.interrupted()) {
					try {
						final FinalBoolean connected = channelConnected;

						if (!connected.value) {
							tryConnect();
						}else{
							Thread.sleep(checkPeriod);
						}
					}catch(Exception e){
						//e.printStackTrace();
					}
				}
			}
		}).start();
	}

	private void tryConnect() throws InterruptedException {
		try {
			lock.lockInterruptibly();
			final FinalWrapper<Bootstrap> b = bootstrap;
			future = new FinalWrapper<ChannelFuture>(b.value.connect(host, port).sync());
			channelConnected = new FinalBoolean(true);
		} finally {
			lock.unlock();
		}
	}

	public ChannelFuture getFuture(){
		FinalWrapper<ChannelFuture> f = future;
		return f.value;
	}

	public boolean isChannelConnected(){
		final FinalBoolean result = channelConnected;
		return result.value;
	}

	public void channelDisconnected() throws InterruptedException {
		try{
			lock.lockInterruptibly();
			channelConnected = new FinalBoolean(false);
		} finally {
			lock.unlock();
		}
	}

	public static interface ConnectionReadyChecker{
		boolean isConnectionReady();
	}
}
