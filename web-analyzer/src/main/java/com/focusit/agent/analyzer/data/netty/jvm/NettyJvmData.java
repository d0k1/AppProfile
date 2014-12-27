package com.focusit.agent.analyzer.data.netty.jvm;

import com.focusit.agent.analyzer.data.netty.NettyData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * Created by Denis V. Kirpichenkov on 28.12.14.
 */
public class NettyJvmData extends NettyData {
	@Override
	protected int getPort() {
		return 16000;
	}

	@Override
	public ChannelHandler getHandler() {
		return new JvmDataServerHandler();
	}

	@Override
	public ReplayingDecoder getDecoder() {
		return null;
	}

	class JvmDataDecoder extends ReplayingDecoder<Void>{

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
			out.add(in);
		}
	}

	class JvmDataServerHandler extends ChannelHandlerAdapter { // (1)

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
			// Discard the received data silently.
			((ByteBuf) msg).release(); // (3)
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
			// Close the connection when an exception is raised.
			cause.printStackTrace();
			ctx.close();
		}
	}
}
