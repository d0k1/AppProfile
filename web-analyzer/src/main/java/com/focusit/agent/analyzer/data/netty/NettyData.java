package com.focusit.agent.analyzer.data.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ReplayingDecoder;

/**
 * Created by Denis V. Kirpichenkov on 28.12.14.
 */
public abstract class NettyData implements Runnable {
	//private final int port = 16000;
	private ChannelFuture f;
	private ChannelHandler handler;

	protected abstract int getPort();

	public void run() {

		EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap(); // (2)
			b.group(bossGroup, workerGroup)
				.channel(NioServerSocketChannel.class) // (3)
				.childHandler(new ChannelInitializer<SocketChannel>() { // (4)
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addLast(getDecoder(), getHandler());
					}
				})
				.option(ChannelOption.SO_BACKLOG, 128)          // (5)
				.childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

			// Bind and start to accept incoming connections.
				f = b.bind(getPort()).sync(); // (7)

			// Wait until the server socket is closed.
			// In this example, this does not happen, but you can do that to gracefully
			// shut down your server.
			f.channel().closeFuture().sync();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
	}

	public void stop() throws InterruptedException {

	}

	public abstract ChannelHandler getHandler();

	public abstract ReplayingDecoder getDecoder();
}
