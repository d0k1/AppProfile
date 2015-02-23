package com.focusit.agent.metrics.dump.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by Denis V. Kirpichenkov on 11.01.15.
 */
public abstract class AbstractNettyDataDumper {

	protected abstract EventLoopGroup getWorkerGroup();

	protected abstract ChannelHandler[] getHandler();

	protected final Bootstrap getBootstrap(){
		Bootstrap b = new Bootstrap();
		b.group(getWorkerGroup());
		b.channel(NioSocketChannel.class);
		b.option(ChannelOption.SO_KEEPALIVE, true);
		b.option(ChannelOption.TCP_NODELAY, true);
		b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
		b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(getHandler());
			}
		});
		return b;
	}
}
