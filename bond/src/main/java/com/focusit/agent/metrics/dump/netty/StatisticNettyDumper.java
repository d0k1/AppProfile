package com.focusit.agent.metrics.dump.netty;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.Statistics;
import com.focusit.agent.metrics.dump.SamplesDataDumper;
import com.focusit.agent.metrics.dump.netty.manager.NettyConnectionManager;
import com.focusit.agent.metrics.samples.ExecutionInfo;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Profiling statistics dumper based on MongoDb
 * Created by Denis V. Kirpichenkov on 09.12.14.
 */
public class StatisticNettyDumper extends AbstractNettyDataDumper implements SamplesDataDumper {
	public static final String PROFILING_STAT_DUMPING_THREAD = "Profiling stat netty dumping thread";
	private static int sampleSize = ExecutionInfo.sizeOf();
	private final int samples = AgentConfiguration.getStatisticsDumpBatch();
	private final Thread dumper;
	private final ByteBuf bytesBuffers[] = new ByteBuf[samples];

	private final ExecutionInfo info = new ExecutionInfo();

	private AtomicLong samplesRead = new AtomicLong(0L);

	private static ChannelFuture lastWrite = null;

	EventLoopGroup workerGroup = new NioEventLoopGroup(0, new NettyThreadFactory("NioEventLoopGroup-statistics-worker"));

	private final static String STATISTICS_TAG = "statistics";

	public StatisticNettyDumper() throws IOException, InterruptedException {

		NettyConnectionManager.getInstance().initConnection(STATISTICS_TAG, getBootstrap(), AgentConfiguration.getNettyDumpStatisticsPort());

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

							if(NettyConnectionManager.getInstance().isConnectionReady(STATISTICS_TAG)) {
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
				NettyConnectionManager.getInstance().disconnected(STATISTICS_TAG);
				super.channelInactive(ctx);
			}
		}};
	}

	private void doDump(boolean checkAvailability) throws InterruptedException {

		int sampleRead = 0;
		InterruptedException interrupted = null;

		for (int i = 0; i < samples; i++) {

			if(checkAvailability) {
				if (!Statistics.hasMore())
					break;
			}

			bytesBuffers[i].clear();
			try {
				Statistics.readData(info);
				info.writeToBuffer(bytesBuffers[i]);
				samplesRead.incrementAndGet();
				sampleRead++;
			} catch (InterruptedException e){
				interrupted = e;
				break;
			}
		}

		if(NettyConnectionManager.getInstance().isConnectionReady(STATISTICS_TAG)) {
			ChannelFuture f = NettyConnectionManager.getInstance().getFuture(STATISTICS_TAG);
			for (int i = 0; i < sampleRead; i++) {
				if (!f.channel().isWritable()) {
					System.err.println("Error: netty statistics channel is not writeable");
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
		if(!NettyConnectionManager.getInstance().isConnectionReady(STATISTICS_TAG))
			return;

		while(Statistics.hasMore()){
			doDump(true);
		}

		ChannelFuture f = NettyConnectionManager.getInstance().getFuture(STATISTICS_TAG);
		f.channel().close().sync();
		workerGroup.shutdownGracefully();
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
		return PROFILING_STAT_DUMPING_THREAD;
	}
}
