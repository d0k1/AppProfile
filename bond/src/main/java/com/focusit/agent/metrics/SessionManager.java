package com.focusit.agent.metrics;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.dump.netty.NettyThreadFactory;
import com.focusit.agent.metrics.dump.netty.manager.NettyConnection;
import com.focusit.agent.metrics.dump.netty.manager.NettyConnectionManager;
import com.focusit.agent.utils.jmm.FinalBoolean;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class to hold information about prifiling session eventId eventId
 * Created by Denis V. Kirpichenkov on 14.12.14.
 */
public class SessionManager {
	private final static String SESSIONMANAGER_TAG = "sessionmanager";
	private final EventLoopGroup workerGroup = new NioEventLoopGroup(0, new NettyThreadFactory("NioEventLoopGroup-session-worker"));
	private final ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.buffer(8);
	private FinalBoolean sessionReady = new FinalBoolean(false);
	private ReentrantLock lock = new ReentrantLock(true);

	public SessionManager() throws InterruptedException {
		Bootstrap b = new Bootstrap(); // (1)
		b.group(workerGroup); // (2)
		b.channel(NioSocketChannel.class); // (3)
		b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
		b.option(ChannelOption.TCP_NODELAY, true);
		b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(getDecoder()).addLast(getHandler());
			}
		});

		NettyConnectionManager.getInstance().initMainConnection(SESSIONMANAGER_TAG, b, AgentConfiguration.getNettySessionPort(), new NettyConnection.ConnectionReadyChecker() {
			@Override
			public boolean isConnectionReady() {
				FinalBoolean result = sessionReady;
				return result.value;
			}
		});
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
			public void channelInactive(ChannelHandlerContext ctx) throws Exception {
				try {
					NettyConnectionManager.getInstance().disconnected(SESSIONMANAGER_TAG);
					super.channelInactive(ctx);
					lock.lock();
					sessionReady = new FinalBoolean(false);
				} finally {
					lock.unlock();
				}
			}

			@Override
			public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
				try{
					lock.lock();
					Boolean ready = false;
					if(msg instanceof Boolean){
						ready = (Boolean) msg;
					}
					sessionReady = new FinalBoolean(ready);
				} finally {
					lock.unlock();
				}
			}
		};
	}

	private ChannelHandler getDecoder(){
		return new ReplayingDecoder<Void>() {
			@Override
			protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
				out.add(in.readBoolean());
			}
		};
	}
}
