package com.focusit.agent.metrics.dump.netty;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.OSMonitoring;
import com.focusit.agent.metrics.dump.SamplesDataDumper;
import com.focusit.agent.metrics.dump.netty.manager.NettyConnectionManager;
import com.focusit.agent.metrics.samples.OSInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Denis V. Kirpichenkov on 28.12.14.
 */
public class OSMonitoringNettyDumper extends AbstractNettyDataDumper implements SamplesDataDumper {
	public static final String OS_MONITORING_DUMPING_THREAD = "OS monitoring netty dumping thread";
	private static final String OS_TAG = "os";

	private static int sampleSize = OSInfo.sizeOf();
	private final int samples = AgentConfiguration.getOsDumpBatch();
	private final Thread dumper;
	private final ByteBuf bytesBuffers[] = new ByteBuf[samples];
	private final OSInfo info = new OSInfo();
	private AtomicLong samplesRead = new AtomicLong(0L);

	EventLoopGroup workerGroup = new NioEventLoopGroup(1, new NettyThreadFactory("NioEventLoopGroup-os-worker"));
	private static ChannelFuture lastWrite = null;

	public OSMonitoringNettyDumper(){
		NettyConnectionManager.getInstance().initConnection(OS_TAG, getBootstrap(), AgentConfiguration.getNettyDumpOSPort());

		for(int i=0;i<samples;i++){
			bytesBuffers[i] = Unpooled.unreleasableBuffer(Unpooled.wrappedBuffer(new byte[sampleSize]));
		}

		dumper = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int interval = AgentConfiguration.getOsMonitoringInterval()*AgentConfiguration.getOsDumpBatch();
					while (!Thread.interrupted()) {
						try {

							doDump(false);

							if(NettyConnectionManager.getInstance().isConnectionReady(OS_TAG)) {
								Thread.yield();
							} else {
								Thread.sleep(interval);
							}
						} catch (InterruptedException e) {
							break;
						}
					}
				} finally {
				}
			}
		}, getName());
		dumper.setDaemon(true);
	}

	@Override
	public void exit() throws InterruptedException {
		dumper.interrupt();
		dumper.join(AgentConfiguration.getThreadJoinTimeout());
	}

	@Override
	public void start() {
		dumper.start();
	}

	@Override
	public long getSamplesRead() {
		return samplesRead.get();
	}

	@Override
	public String getName() {
		return OS_MONITORING_DUMPING_THREAD;
	}

	@Override
	protected EventLoopGroup getWorkerGroup() {
		return workerGroup;
	}

	protected final ChannelHandler[] getHandler() {
		return new ChannelHandlerAdapter[]{new ChannelHandlerAdapter(){
			@Override
			public void channelInactive(ChannelHandlerContext ctx) throws Exception {
				NettyConnectionManager.getInstance().disconnected(OS_TAG);
				super.channelInactive(ctx);
			}
		}};
	}
	private void doDump(boolean checkAvailability) throws InterruptedException {

		int sampleRead = 0;
		InterruptedException interrupted = null;

		for (int i = 0; i < samples; i++) {

			if(checkAvailability) {
				if (!OSMonitoring.hasMore())
					break;
			}

			bytesBuffers[i].clear();
			try {
				OSMonitoring.readData(info);
				info.writeToBuffer(bytesBuffers[i]);
				samplesRead.incrementAndGet();
				sampleRead++;
			} catch (InterruptedException e){
				interrupted = e;
				break;
			}
		}

		if(NettyConnectionManager.getInstance().isConnectionReady(OS_TAG)) {
			ChannelFuture f = NettyConnectionManager.getInstance().getFuture(OS_TAG);

			for (int i = 0; i < sampleRead; i++) {
				bytesBuffers[i].resetReaderIndex();
				lastWrite = f.channel().writeAndFlush(bytesBuffers[i]);
			}

			f.channel().flush();
			if (lastWrite != null) {
				lastWrite.sync();
				lastWrite = null;
			}
		}
		if(interrupted!=null) {
			throw interrupted;
		}
	}

	@Override
	public final void dumpRest() throws InterruptedException {
		if(!NettyConnectionManager.getInstance().isConnectionReady(OS_TAG))
			return;

		try {
			OSMonitoring.getInstance().doMeasureAtExit();
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (OSMonitoring.hasMore()) {
			doDump(true);
		}

		ChannelFuture f = NettyConnectionManager.getInstance().getFuture(OS_TAG);
		f.channel().close();
		f.channel().closeFuture().sync();
		workerGroup.shutdownGracefully().sync();
	}
}
