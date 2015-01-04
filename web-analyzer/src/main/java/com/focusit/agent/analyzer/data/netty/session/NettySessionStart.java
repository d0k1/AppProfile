package com.focusit.agent.analyzer.data.netty.session;

import com.focusit.agent.analyzer.data.netty.NettyData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

/**
 * Created by Denis V. Kirpichenkov on 02.01.15.
 */
public class NettySessionStart extends NettyData {
	private final NettySessionManager manager;

	public NettySessionStart(NettySessionManager manager){
		this.manager = manager;
	}

	@Override
	protected int getPort() {
		return 15999;
	}

	@Override
	protected String getName() {
		return "sessionControl";
	}

	@Override
	public ChannelHandler getHandler() {
		return new ChannelHandlerAdapter(){
			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				Long appId = (Long) msg;
				ctx.attr(AttributeKey.valueOf("appId")).set(appId);
				manager.onSesionStart(appId);
				ReferenceCountUtil.release(msg);
			}

			@Override
			public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
				super.disconnect(ctx, promise);
				manager.onSessionStop((Long) ctx.attr(AttributeKey.valueOf("appId")).get());
			}
		};
	}

	@Override
	public ChannelHandler[] getDecoder() {
		return new ChannelHandler[]{new ReplayingDecoder<Void>() {
			@Override
			protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
				out.add(in.readLong());
			}
		}};
	}
}
