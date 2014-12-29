package com.focusit.agent.analyzer.data.netty.jvm;

import com.focusit.agent.analyzer.data.netty.NettyData;
import com.focusit.agent.metrics.samples.JvmInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.ReferenceCountUtil;

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
	protected String getName() {
		return "jvm";
	}

	@Override
	public ChannelHandler getHandler() {
		return new JvmDataServerHandler();
	}

	@Override
	public ChannelHandler[] getDecoder() {
		return new JvmDataDecoder[]{new JvmDataDecoder()};
	}

	class JvmDataDecoder extends ReplayingDecoder<Void>{

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
			JvmInfo info = new JvmInfo();
			info.readFromBuffer(in);
			out.add(info);
		}
	}

	class JvmDataServerHandler extends ChannelHandlerAdapter {

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
			System.err.println(msg.getClass().getName());
			ReferenceCountUtil.release(msg);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			cause.printStackTrace();
			ctx.close();
		}
	}
}
