package com.focusit.agent.metrics.dump.netty;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.Statistics;
import com.focusit.agent.metrics.dump.SamplesDataDumper;
import com.focusit.agent.metrics.samples.ExecutionInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Profiling statistics dumper based on MongoDb
 * Created by Denis V. Kirpichenkov on 09.12.14.
 */
public class StatisticNettyDumper implements SamplesDataDumper {
	public static final String PROFILING_STAT_DUMPING_THREAD = "Profiling stat netty dumping thread";
	private static int sampleSize = ExecutionInfo.sizeOf();
	private final int samples = AgentConfiguration.getStatisticsDumpBatch();
	private final Thread dumper;
	private final ByteBuf bytesBuffers[] = new ByteBuf[samples];

	private final ExecutionInfo info = new ExecutionInfo();

	private AtomicLong samplesRead = new AtomicLong(0L);

	private static final int port = Integer.parseInt(AgentConfiguration.getNettyDumpStatisticsPort());
	private static ChannelFuture lastWrite = null;

	EventLoopGroup workerGroup = new NioEventLoopGroup(0, new NettyThreadFactory("NioEventLoopGroup-statistics-worker"));
	ChannelFuture f;

	public StatisticNettyDumper() throws IOException, InterruptedException {

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
		f = b.connect(AgentConfiguration.getNettyDumpHost(), port).sync(); // (5)


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
							Thread.yield();
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

	private ChannelHandler getHandler() {
		return new ChannelHandlerAdapter();
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

		for(int i=0;i<sampleRead;i++) {
			if(!f.channel().isWritable()){
				System.err.println("Error: netty statistics channel is not writeable");
				break;
			}
			//nettyBuffer.resetWriterIndex();
			bytesBuffers[i].resetReaderIndex();
			lastWrite = f.channel().write(bytesBuffers[i]);
			//nettyBuffer.clear();
		}
		f.channel().flush();
		if(lastWrite!=null){
			lastWrite.sync();
			lastWrite = null;
		}
		if(interrupted!=null) {
			throw interrupted;
		}
	}

	@Override
	public final void dumpRest() throws InterruptedException {
		while(Statistics.hasMore()){
			doDump(true);
		}

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
