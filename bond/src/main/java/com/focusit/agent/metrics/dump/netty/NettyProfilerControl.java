package com.focusit.agent.metrics.dump.netty;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.dump.netty.manager.NettyConnectionManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * Created by Denis V. Kirpichenkov on 23.02.15.
 */
public class NettyProfilerControl {
	private final static String PROFILER_TAG = "profiler";
	private final EventLoopGroup workerGroup = new NioEventLoopGroup(0, new NettyThreadFactory("NioEventLoopGroup-profiler-worker"));
	private final ByteBuf byteBuf = Unpooled.unreleasableBuffer(UnpooledByteBufAllocator.DEFAULT.buffer(8));

	public NettyProfilerControl() {
		Bootstrap b = new Bootstrap();
		b.group(workerGroup);
		b.channel(NioSocketChannel.class);
		b.option(ChannelOption.SO_KEEPALIVE, true);
		b.option(ChannelOption.TCP_NODELAY, true);
		b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(getDecoder()).addLast(getHandler());
			}
		});

		NettyConnectionManager.getInstance().initConnection(PROFILER_TAG, b, AgentConfiguration.getNettyProfilerPort());
	}

	private ChannelHandler getHandler() {
		return new ChannelHandlerAdapter() {
			@Override
			public void channelActive(ChannelHandlerContext ctx) throws Exception {
				byteBuf.resetReaderIndex();
				byteBuf.resetWriterIndex();
				byteBuf.writeLong(AgentConfiguration.getAppId());
				ctx.writeAndFlush(byteBuf);
			}

			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				Long commandId = (Long) msg;
				System.err.println("Received command " + commandId);
			}
		};
	}

	private ChannelHandler getDecoder(){
		return new ReplayingDecoder<Void>() {
			@Override
			protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
				long command = in.readLong();
				out.add(command);
			}
		};
	}
}

