package com.focusit.agent.analyzer.data.netty.profiler;

import com.focusit.agent.analyzer.data.netty.NettyData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Denis V. Kirpichenkov on 24.02.15.
 */
@Component
public class ProfilerControl extends NettyData {

	private ConcurrentHashMap<Long, ChannelHandlerContext> appChannels = new ConcurrentHashMap<>();

	@Override
	protected int getPort() {
		return 16002;
	}

	@Override
	protected String getName() {
		return "profiler";
	}

	@Override
	public ChannelHandler getHandler() {
		return new ChannelHandlerAdapter(){
			@Override
			public void channelActive(ChannelHandlerContext ctx) throws Exception {
				super.channelActive(ctx);
			}

			@Override
			public void channelInactive(ChannelHandlerContext ctx) throws Exception {
				super.channelInactive(ctx);
			}

			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				super.channelRead(ctx, msg);
			}
		};
	}

	@Override
	public ChannelHandler[] getDecoder() {
		return new ChannelHandler[]{new ReplayingDecoder<Void>(){
			@Override
			protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
				Long mark = in.readLong();
				if(mark>0){
					System.out.println("appId received: "+mark);
					appChannels.put(mark, ctx);
				}
			}
		}};
	}
}
