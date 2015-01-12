package com.focusit.agent.metrics.dump.netty;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.MethodsMap;
import com.focusit.agent.metrics.dump.SamplesDataDumper;
import com.focusit.agent.metrics.dump.netty.manager.NettyConnectionManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
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
public class MethodsMapNettyDumper extends AbstractNettyDataDumper implements SamplesDataDumper {
	public static final String METHOD_MAP_DUMPING_THREAD = "MethodMap netty dumping thread";
	private long lastIndex = 0;
	private final Thread dumper;
	private final Charset cs = Charset.forName("UTF-8");
	private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

	private AtomicLong samplesRead = new AtomicLong(0L);

	private static ChannelFuture lastWrite = null;

	EventLoopGroup workerGroup = new NioEventLoopGroup(0, new NettyThreadFactory("NioEventLoopGroup-methodmap-worker"));

	private static final String METHODSMAP_TAG = "methodsmap";

	public MethodsMapNettyDumper() throws IOException, InterruptedException {

		NettyConnectionManager.getInstance().initConnection(METHODSMAP_TAG, getBootstrap(), AgentConfiguration.getNettyDumpMethodMapPort());

		dumper = new Thread(new Runnable() {
			@Override
			public void run() {
				int interval = AgentConfiguration.getDumpInterval();
				while (!Thread.interrupted()) {

					try {
						if(NettyConnectionManager.getInstance().isConnectionReady(METHODSMAP_TAG)) {
							while (lastIndex < MethodsMap.getLastIndex()) {
								doDump();
							}
							waitToCompleteWrite();
						}
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
		if(!NettyConnectionManager.getInstance().isConnectionReady(METHODSMAP_TAG))
			return;

		ChannelFuture f = NettyConnectionManager.getInstance().getFuture(METHODSMAP_TAG);

		if (!f.channel().isWritable())
			return;

		f.channel().flush();
		if(lastWrite!=null){
			lastWrite.sync();
			lastWrite = null;
		}
	}

	@Override
	protected EventLoopGroup getWorkerGroup() {
		return workerGroup;
	}

	protected final ChannelHandler[] getHandler() {
		return new ChannelHandler[]{new MethodsMapSampleEncoder(), new ChannelHandlerAdapter(){
			@Override
			public void channelInactive(ChannelHandlerContext ctx) throws Exception {
				NettyConnectionManager.getInstance().disconnected(METHODSMAP_TAG);
				super.channelInactive(ctx);
			}
		}};
	}

	@Override
	public final void exit() throws InterruptedException {
		dumper.interrupt();
		dumper.join(AgentConfiguration.getThreadJoinTimeout());
	}

	@Override
	public final void dumpRest() {
		if(!NettyConnectionManager.getInstance().isConnectionReady(METHODSMAP_TAG))
			return;

		while(lastIndex<MethodsMap.getLastIndex()){
			doDump();
		}
		try {
			waitToCompleteWrite();
			ChannelFuture f = NettyConnectionManager.getInstance().getFuture(METHODSMAP_TAG);
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

			if(NettyConnectionManager.getInstance().isConnectionReady(METHODSMAP_TAG)) {
				ChannelFuture f = NettyConnectionManager.getInstance().getFuture(METHODSMAP_TAG);
				if (f.channel().isWritable()) {
					lastWrite = f.channel().write(new MethodsMapSample(lastIndex, MethodsMap.getMethod((int) lastIndex)));
				}
				lastIndex++;
				samplesRead.incrementAndGet();
			}
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
