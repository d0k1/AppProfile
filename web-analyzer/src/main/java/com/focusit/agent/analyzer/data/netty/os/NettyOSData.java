package com.focusit.agent.analyzer.data.netty.os;

import com.focusit.agent.analyzer.data.netty.NettyData;
import com.focusit.agent.metrics.samples.OSInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

/**
 * Created by Denis V. Kirpichenkov on 23.02.15.
 */
public class NettyOSData extends NettyData {
	private final OSDataImport dataImport;

	@Override
	protected int getPort() {
		return 16001;
	}

	@Override
	protected String getName() {
		return "os";
	}

	public NettyOSData(OSDataImport dataImport){
		this.dataImport = dataImport;
	}

	@Override
	public ChannelHandler getHandler() {
		return new OSDataServerHandler();
	}

	@Override
	public ChannelHandler[] getDecoder() {
		return new OSDataDecoder[]{new OSDataDecoder()};
	}

	class OSDataDecoder extends ReplayingDecoder<Void> {

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
			OSInfo info = new OSInfo();
			info.readFromBuffer(in);
			out.add(info);
		}
	}

	class OSDataServerHandler extends ChannelHandlerAdapter {

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
			OSInfo sample = (OSInfo) msg;
			dataImport.importSample(sample.appId, sample);
			ReferenceCountUtil.release(msg);
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			cause.printStackTrace();
			ctx.close();
		}
	}
}
