package com.focusit.agent.analyzer.data.netty.statistics;

import com.focusit.agent.analyzer.data.netty.NettyData;
import com.focusit.agent.metrics.samples.ExecutionInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

/**
 * Created by Denis V. Kirpichenkov on 29.12.14.
 */
public class NettyStatisticsData  extends NettyData {
	@Override
	protected int getPort() {
		return 16003;
	}

	@Override
	protected String getName() {
		return "statistics";
	}

	@Override
	public ChannelHandler getHandler() {
		return new ExecutionDataServerHandler();
	}

	@Override
	public ChannelHandler[] getDecoder() {
		return new ExecutionDataDecoder[]{new ExecutionDataDecoder()};
	}

	class ExecutionDataDecoder extends ReplayingDecoder<Void> {

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
			ExecutionInfo info = new ExecutionInfo();
			info.readFromBuffer(in);
			out.add(info);
		}
	}

	class ExecutionDataServerHandler extends ChannelHandlerAdapter {

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
