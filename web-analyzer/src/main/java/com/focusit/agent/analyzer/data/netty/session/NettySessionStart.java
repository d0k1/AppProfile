package com.focusit.agent.analyzer.data.netty.session;

import com.focusit.agent.analyzer.data.netty.DataImport;
import com.focusit.agent.analyzer.data.netty.NettyData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

/**
 * Created by Denis V. Kirpichenkov on 02.01.15.
 */
public class NettySessionStart extends NettyData {
	private final DataImport dataImport[];

	public NettySessionStart(DataImport ... dataImport){
		this.dataImport = dataImport;
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

				for(DataImport onStart:dataImport) {
					onStart.startNewSession(appId);
				}
				ReferenceCountUtil.release(msg);
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
