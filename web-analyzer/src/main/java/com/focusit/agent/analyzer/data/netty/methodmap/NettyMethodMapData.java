package com.focusit.agent.analyzer.data.netty.methodmap;

import com.focusit.agent.analyzer.data.netty.NettyData;
import com.focusit.agent.metrics.dump.netty.MethodsMapNettyDumper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.ReferenceCountUtil;

import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by Denis V. Kirpichenkov on 28.12.14.
 */
public class NettyMethodMapData extends NettyData {
	private final MethodMapImport dataImport;

	public NettyMethodMapData(MethodMapImport dataImport){
		this.dataImport = dataImport;
	}

	@Override
	protected int getPort() {
		return 16002;
	}

	@Override
	protected String getName() {
		return "methodsmap";
	}

	@Override
	public ChannelHandler getHandler() {
		return new ChannelHandlerAdapter(){
			@Override
			public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
				super.connect(ctx, remoteAddress, localAddress, promise);
			}

			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				MethodsMapNettyDumper.MethodsMapSample sample = (MethodsMapNettyDumper.MethodsMapSample)msg;
				dataImport.importSample(sample.appId, sample);
				ReferenceCountUtil.release(msg);
			}

			@Override
			public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
				cause.printStackTrace();
				super.exceptionCaught(ctx, cause);
			}
		};
	}

	@Override
	public ChannelHandler[] getDecoder() {
		return new ChannelHandler[]{new MethodsMapSampleDecoder()};
	}

	private class MethodsMapSampleDecoder extends ReplayingDecoder<Void> {
		private final Charset charset = Charset.forName("UTF-8");

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
			long appId = msg.readLong();
			long index = msg.readLong();
			int dataSize = msg.readInt();
			byte data[] = new byte[dataSize];
			msg.readBytes(data);

			out.add(new MethodsMapNettyDumper.MethodsMapSample(appId, index, new String(data, charset)));
		}
	}
}
