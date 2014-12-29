package com.focusit.agent.metrics.dump.netty;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.MethodsMap;
import com.focusit.agent.metrics.dump.SamplesDataDumper;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Methods map dumper storaged based on MongoDb
 * Created by Denis V. Kirpichenkov on 09.12.14.
 */
public class MethodsMapNettyDumper implements SamplesDataDumper {
	public static final String METHOD_MAP_DUMPING_THREAD = "MethodMap netty dumping thread";
	private long lastIndex = 0;
	private final Thread dumper;
	private final Charset cs = Charset.forName("UTF-8");
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

	private AtomicLong samplesRead = new AtomicLong(0L);

	private static final int port = Integer.parseInt(AgentConfiguration.getNettyDumpMethodMapPort());
	private static ChannelFuture lastWrite = null;

	EventLoopGroup workerGroup = new NioEventLoopGroup(0, new NettyThreadFactory("NioEventLoopGroup-methodmap-worker"));
	ChannelFuture f;

	public MethodsMapNettyDumper() throws IOException, InterruptedException {
		Bootstrap b = new Bootstrap(); // (1)
		b.group(workerGroup); // (2)
		b.channel(NioSocketChannel.class); // (3)
		b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
		b.option(ChannelOption.TCP_NODELAY, true);
		b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
				ch.pipeline().addLast(new MethodsMapSampleEncoder(), getHandler());
			}
		});
		f = b.connect(AgentConfiguration.getNettyDumpHost(), port).sync(); // (5)

		dumper = new Thread(new Runnable() {
			@Override
			public void run() {
				int interval = AgentConfiguration.getDumpInterval();
				while (!Thread.interrupted()) {

					try {
						while(lastIndex< MethodsMap.getLastIndex()){
							doDump();
						}
						waitToCompleteWrite();
						Thread.sleep(interval);

					} catch (InterruptedException e) {
						break;
					}
				}
			}
		}, getName());

		dumper.setDaemon(true);
	}

	private void waitToCompleteWrite() throws InterruptedException {
		f.channel().flush();
		if(lastWrite!=null){
			lastWrite.sync();
			lastWrite = null;
		}
	}

	private ChannelHandler getHandler() {
		return new ChannelHandlerAdapter();
	}

	@Override
	public final void exit() throws InterruptedException {
		dumper.interrupt();
		dumper.join(AgentConfiguration.getThreadJoinTimeout());
	}

	@Override
	public final void dumpRest() {
		while(lastIndex<MethodsMap.getLastIndex()){
			doDump();
		}
		try {
			waitToCompleteWrite();
			f.channel().close();
			f.channel().closeFuture().sync();
			workerGroup.shutdownGracefully().sync();
		} catch (InterruptedException e) {
			System.err.println("Error MethodMap netty dump rest");
		}
	}

	private void doDump() {
		try {
			readWriteLock.readLock().lock();

			if (lastIndex >= MethodsMap.getLastIndex())
				return;

				lastWrite = f.channel().write(new MethodsMapSample(lastIndex, MethodsMap.getMethod((int) lastIndex)));
				lastIndex++;
				samplesRead.incrementAndGet();
		}finally{
			readWriteLock.readLock().unlock();
		}
	}

	@Override
	public final void start() {
		dumper.start();
	}

	@Override
	public long getSamplesRead() {
		return samplesRead.get();
	}

	@Override
	public String getName() {
		return METHOD_MAP_DUMPING_THREAD;
	}

	public static class MethodsMapSample{
		public final long appId;
		public final long index;
		public final String method;

		@Override
		public String toString() {
			return "MethodsMapSample{" +
				"appId=" + appId +
				", index=" + index +
				", method='" + method + '\'' +
				'}';
		}

		public MethodsMapSample(long appId, long index, String method) {
			this.appId = appId;
			this.index = index;
			this.method = method;
		}

		public MethodsMapSample(long index, String method) {
			this.appId = AgentConfiguration.getAppId();
			this.index = index;
			this.method = method;
		}
	}

	private class MethodsMapSampleEncoder extends MessageToMessageEncoder<MethodsMapSample> {
		private final Charset charset = Charset.forName("UTF-8");
		@Override
		protected void encode(ChannelHandlerContext ctx, MethodsMapSample msg, List<Object> out) throws Exception {
			byte data[] = msg.method.getBytes(charset);
			ByteBuf buffer = ctx.alloc().heapBuffer(data.length+8+8+4);
			buffer.writeLong(msg.appId);
			buffer.writeLong(msg.index);
			buffer.writeInt(data.length);
			buffer.writeBytes(data);
			out.add(buffer);
		}
	}
}
