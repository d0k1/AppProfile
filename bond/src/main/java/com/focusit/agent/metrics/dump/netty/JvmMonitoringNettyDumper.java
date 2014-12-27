package com.focusit.agent.metrics.dump.netty;

import com.focusit.agent.bond.AgentConfiguration;
import com.focusit.agent.metrics.JvmMonitoring;
import com.focusit.agent.metrics.dump.SamplesDataDumper;
import com.focusit.agent.metrics.samples.JvmInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Denis V. Kirpichenkov on 10.12.14.
 */
public class JvmMonitoringNettyDumper implements SamplesDataDumper {
	public static final String JVM_MONITORING_DUMPING_THREAD = "Jvm monitoring netty dumping thread";
	private static int sampleSize = JvmInfo.sizeOf();
	private final int samples = AgentConfiguration.getJvmDumpBatch();
	private final Thread dumper;
	private final ByteBuffer bytesBuffers[] = new ByteBuffer[samples];

//	private final RandomAccessFile aFile;
//	private final FileChannel channel;
	private final JvmInfo info = new JvmInfo();

	private AtomicLong samplesRead = new AtomicLong(0L);

	private static final int port = Integer.parseInt(AgentConfiguration.getNettyDumpJvmPort());

	EventLoopGroup workerGroup = new NioEventLoopGroup();
	ChannelFuture f;

	public JvmMonitoringNettyDumper() throws IOException, InterruptedException {

		Bootstrap b = new Bootstrap(); // (1)
		b.group(workerGroup); // (2)
		b.channel(NioSocketChannel.class); // (3)
		b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
		b.handler(new ChannelInitializer<SocketChannel>() {
			@Override
			public void initChannel(SocketChannel ch) throws Exception {
//					ch.pipeline().addLast(new TimeClientHandler());
			}
		});

		// Start the client.
		f = b.connect(AgentConfiguration.getNettyDumpHost(), port).sync(); // (5)

//		channel = FileChannel.open(FileSystems.getDefault().getPath(file), EnumSet.of(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));
//		aFile = new RandomAccessFile(AgentConfiguration.getJvmMonitoringFile(), "rw");
//		channel = aFile.getChannel();
//		channel.truncate(0);

		for(int i=0;i<samples;i++){
			bytesBuffers[i] = ByteBuffer.allocate(sampleSize);
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
//		dumper.setPriority(Thread.MAX_PRIORITY);
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
				bytesBuffers[i].flip();
				sampleRead++;
			} catch (InterruptedException e){
				interrupted = e;
				break;
			}
		}

		for(int i=0;i<sampleRead;i++) {
			f.channel().write(bytesBuffers[i].array());
		}
		f.channel().flush();
		if(interrupted!=null) {
			throw interrupted;
		}
	}

	@Override
	public final void dumpRest() throws InterruptedException {
		JvmMonitoring.getInstance().doMeasureAtExit();

		while (JvmMonitoring.hasMore()) {
			doDump(true);
		}

		f.channel().closeFuture().sync();
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
		return JVM_MONITORING_DUMPING_THREAD;
	}
}
