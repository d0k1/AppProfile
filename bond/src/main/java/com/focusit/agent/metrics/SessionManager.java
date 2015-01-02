package com.focusit.agent.metrics;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.dump.netty.NettyThreadFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Class to hold information about prifiling session eventId eventId
 * Created by Denis V. Kirpichenkov on 14.12.14.
 */
public class SessionManager {
	EventLoopGroup workerGroup = new NioEventLoopGroup(0, new NettyThreadFactory("NioEventLoopGroup-session-worker"));
	ChannelFuture f;


	public SessionManager() throws InterruptedException {
		Bootstrap b = new Bootstrap(); // (1)
		b.group(workerGroup); // (2)
		b.channel(NioSocketChannel.class); // (3)
		b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
		b.option(ChannelOption.TCP_NODELAY, true);
		b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(getHandler());
			}
		});
		f = b.connect(AgentConfiguration.getNettyDumpHost(), Integer.parseInt(AgentConfiguration.getNettySessionPort())).sync();
	}

	public void start() throws InterruptedException {
		if(f.channel().isWritable()) {
			ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer(8);
			byteBuf.writeLong(AgentConfiguration.getAppId());
			f.channel().writeAndFlush(byteBuf).sync();
		}
	}

	private ChannelHandler getHandler() {
		return new ChannelHandlerAdapter();
	}
}
