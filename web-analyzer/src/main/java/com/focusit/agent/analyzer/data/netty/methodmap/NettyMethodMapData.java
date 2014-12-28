package com.focusit.agent.analyzer.data.netty.methodmap;

import com.focusit.agent.analyzer.data.netty.NettyData;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by Denis V. Kirpichenkov on 28.12.14.
 */
public class NettyMethodMapData extends NettyData {
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
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				System.err.println((String)msg);
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
		return new ChannelHandler[]{new DelimiterBasedFrameDecoder(16535, Delimiters.nulDelimiter()), new StringDecoder()};
	}

}
