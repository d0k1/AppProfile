package com.focusit.agent.metrics.dump.netty.manager;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.dump.netty.NettyThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Class to manage netty connections.
 * Created by Denis V. Kirpichenkov on 10.01.15.
 */
public class NettyConnectionManager {
	private static final NettyConnectionManager instance = new NettyConnectionManager();
	// Netty settings: host and ports for required channels
	private final String server = AgentConfiguration.getNettyDumpHost();
	private final int checkPeriod = AgentConfiguration.getNettyConnectingInterval();
	private final NettyThreadFactory threadFactory = new NettyThreadFactory("Netty-connection-checker-threads");

	private final ConcurrentHashMap<String, NettyConnection> connections = new ConcurrentHashMap<>();

	private NettyConnectionManager(){

	}

	public static NettyConnectionManager getInstance(){
		return instance;
	}

	public void initConnection(String name, Bootstrap b, int port){
		if(connections.contains(name)) {
			System.err.println("Channel already initialized");
			return;
		}

		NettyConnection connection = new NettyConnection(name, server, port, checkPeriod, threadFactory);
		connections.put(name, connection);
		connection.init(b);
	}

	public boolean isConnected(String name){
		final boolean result = connections.get(name).isChannelConnected();
		return result;
	}

	public void disconnected(String name) throws InterruptedException {
		connections.get(name).channelDisconnected();
	}

	public ChannelFuture getFuture(String name){
		return connections.get(name).getFuture();
	}
}
