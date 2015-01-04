package com.focusit.agent.analyzer.data.netty;

import com.focusit.agent.metrics.dump.netty.NettyThreadFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Base class to interact with agent through netty
 * Created by Denis V. Kirpichenkov on 28.12.14.
 */
public abstract class NettyData implements Runnable {
	private ChannelFuture f;

	protected abstract int getPort();

	protected abstract String getName();

	public void run() {

		EventLoopGroup bossGroup = new NioEventLoopGroup(0, new NettyThreadFactory("NioEventLoopGroup-"+getName()+"-boss"));
		EventLoopGroup workerGroup = new NioEventLoopGroup(0, new NettyThreadFactory("NioEventLoopGroup-"+getName()+"-worker"));
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<SocketChannel>() { // (4)
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(getDecoder()).addLast(getHandler());
					}
				})
				.option(ChannelOption.SO_BACKLOG, 128)
				.childOption(ChannelOption.SO_KEEPALIVE, true);

				f = b.bind(getPort()).sync();

			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
	}

	public abstract ChannelHandler getHandler();

	public abstract ChannelHandler[] getDecoder();
}
