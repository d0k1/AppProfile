package com.focusit.agent.metrics.dump.netty;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.JvmMonitoring;
import com.focusit.agent.metrics.dump.SamplesDataDumper;
import com.focusit.agent.metrics.dump.netty.manager.NettyConnectionManager;
import com.focusit.agent.metrics.samples.JvmInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Denis V. Kirpichenkov on 10.12.14.
 */
public class JvmMonitoringNettyDumper extends AbstractNettyDataDumper implements SamplesDataDumper {
	public static final String JVM_MONITORING_DUMPING_THREAD = "Jvm monitoring netty dumping thread";
	private static final String JVM_TAG = "jvm";
	private static int sampleSize = JvmInfo.sizeOf();
	private final int samples = AgentConfiguration.getJvmDumpBatch();
	private final Thread dumper;
	private final ByteBuf bytesBuffers[] = new ByteBuf[samples];

	private final JvmInfo info = new JvmInfo();

	private AtomicLong samplesRead = new AtomicLong(0L);
	private static ChannelFuture lastWrite = null;

	EventLoopGroup workerGroup = new NioEventLoopGroup(0, new NettyThreadFactory("NioEventLoopGroup-jvm-worker"));

	public JvmMonitoringNettyDumper() throws IOException, InterruptedException {

		NettyConnectionManager.getInstance().initConnection(JVM_TAG, getBootstrap(), AgentConfiguration.getNettyDumpJvmPort());

		for(int i=0;i<samples;i++){
			bytesBuffers[i] = Unpooled.unreleasableBuffer(Unpooled.wrappedBuffer(new byte[sampleSize]));
		}

		dumper = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					int interval = AgentConfiguration.getDumpInterval();
					while (!Thread.interrupted()) {
						try {

							doDump(false);

							if(NettyConnectionManager.getInstance().isConnectionReady(JVM_TAG)) {
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
	protected EventLoopGroup getWorkerGroup() {
		return workerGroup;
	}

	protected final ChannelHandler[] getHandler() {
		return new ChannelHandlerAdapter[]{new ChannelHandlerAdapter(){
			@Override
			public void channelInactive(ChannelHandlerContext ctx) throws Exception {
				NettyConnectionManager.getInstance().disconnected(JVM_TAG);
				super.channelInactive(ctx);
			}
		}};
	}

	private void doDump(boolean checkAvailability) throws InterruptedException {

		int sampleRead = 0;
		InterruptedException interrupted = null;

		for (int i = 0; i < samples; i++) {

			if(checkAvailability) {
				if (!JvmMonitoring.hasMore())
					break;
			}

			bytesBuffers[i].clear();
			try {
				JvmMonitoring.readData(info);
				info.writeToBuffer(bytesBuffers[i]);
				samplesRead.incrementAndGet();
				sampleRead++;
			} catch (InterruptedException e){
				interrupted = e;
				break;
			}
		}

		if(NettyConnectionManager.getInstance().isConnectionReady(JVM_TAG)) {
			ChannelFuture f = NettyConnectionManager.getInstance().getFuture(JVM_TAG);

			for (int i = 0; i < sampleRead; i++) {
				if (!f.channel().isWritable()) {
					System.err.println("Error: netty jvm monitoring channel is not writeable");
					break;
				}
				bytesBuffers[i].resetReaderIndex();
				lastWrite = f.channel().write(bytesBuffers[i]);
			}
			if (f.channel().isWritable()) {
				f.channel().flush();
				if (lastWrite != null) {
					lastWrite.sync();
					lastWrite = null;
				}
			}
		}
		if(interrupted!=null) {
			throw interrupted;
		}
	}

	@Override
	public final void dumpRest() throws InterruptedException {
		if(!NettyConnectionManager.getInstance().isConnectionReady(JVM_TAG))
			return;

		JvmMonitoring.getInstance().doMeasureAtExit();
		while (JvmMonitoring.hasMore()) {
			doDump(true);
		}

		ChannelFuture f = NettyConnectionManager.getInstance().getFuture(JVM_TAG);
		f.channel().close();
		f.channel().closeFuture().sync();
		workerGroup.shutdownGracefully().sync();
	}

	@Override
	public final void exit() throws InterruptedException {
		dumper.interrupt();
		dumper.join(AgentConfiguration.getThreadJoinTimeout());
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
		return JVM_MONITORING_DUMPING_THREAD;
	}
}
