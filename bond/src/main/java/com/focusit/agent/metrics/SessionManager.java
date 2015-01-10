package com.focusit.agent.metrics;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.dump.netty.NettyThreadFactory;
import com.focusit.agent.metrics.dump.netty.manager.NettyConnectionManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Class to hold information about prifiling session eventId eventId
 * Created by Denis V. Kirpichenkov on 14.12.14.
 */
public class SessionManager {
	private final static String SESSIONMANAGER_TAG = "sessionmanager";
	private final EventLoopGroup workerGroup = new NioEventLoopGroup(0, new NettyThreadFactory("NioEventLoopGroup-session-worker"));
	private final ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer(8);

	public SessionManager() throws InterruptedException {
		Bootstrap b = new Bootstrap(); // (1)
		b.group(workerGroup); // (2)
		b.channel(NioSocketChannel.class); // (3)
		b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
		b.option(ChannelOption.TCP_NODELAY, true);
		b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(getHandler());
			}
		});

		NettyConnectionManager.getInstance().initConnection(SESSIONMANAGER_TAG, b, AgentConfiguration.getNettySessionPort());
	}

	public void start(){
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int interval = AgentConfiguration.getDumpInterval();
					boolean lastConnectionStatus = false;

					while (!Thread.interrupted()) {
						try {
							boolean connected = NettyConnectionManager.getInstance().isConnected(SESSIONMANAGER_TAG);
							if(connected && !lastConnectionStatus) {
								byteBuf.resetReaderIndex();
								byteBuf.resetWriterIndex();
								byteBuf.writeLong(AgentConfiguration.getAppId());
								lastConnectionStatus = connected;
								NettyConnectionManager.getInstance().getFuture(SESSIONMANAGER_TAG).channel().writeAndFlush(byteBuf).sync();
							} else if(!connected){
								lastConnectionStatus = connected;
								Thread.sleep(interval);
							}
						} catch (InterruptedException e) {
							break;
						}
					}
				} finally {
				}
			}
		}, "Netty session manager");
		thread.setDaemon(true);
		thread.start();
	}

	private ChannelHandler getHandler() {
		return new ChannelHandlerAdapter(){
			@Override
			public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
				NettyConnectionManager.getInstance().disconnected(SESSIONMANAGER_TAG);
				super.disconnect(ctx, promise);
			}
		};
	}
}
