package com.focusit.agent.mongo;

import com.focusit.agent.metrics.samples.ExecutionInfo;
import com.focusit.agent.metrics.samples.JvmInfo;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

/**
 * Move to netty jvm monitoring data
 * Created by Denis V. Kirpichenkov on 14.12.14.
 */
public class JvmMonitoringLoader implements MongoLoader {
	private final static Logger LOG = Logger.getLogger(JvmMonitoringLoader.class.getName());

	@Override
	public void loadData(DB bondDb, long sessionId) {
		String file = MongoConfiguration.getJvmMonitoringFile();
		DBCollection collection = bondDb.getCollection(MongoConfiguration.getJvmMonitoringCollection());

		int samples = 40000;
		ByteBuffer buffer = ByteBuffer.allocate(samples* ExecutionInfo.sizeOf());
		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {

			try(FileChannel ch = raf.getChannel()) {

				int content;
				long index = 0;
				while ((content = ch.read(buffer)) != -1) {
					if (content < JvmInfo.sizeOf()) {
						throw new IOException("Error reading JvmInfo");
					}

					int count = content / JvmInfo.sizeOf();

					JvmInfo info = new JvmInfo();

					buffer.flip();
					BulkWriteOperation ops = collection.initializeUnorderedBulkOperation();

					for (int i = 0; i < count; i++) {
						info.readFromBuffer(buffer);

						BasicDBObject jvmInfo = new BasicDBObject("sessionId", sessionId);
						jvmInfo.append("sample", index+i);
						if (info.ipv4Addr1 != 0) {
							jvmInfo.append("ipv4Addr1", InetAddress.getByAddress(BigInteger.valueOf(info.ipv4Addr1).toByteArray()).getHostAddress());
						}
						if (info.ipv4Addr2 != 0) {
							jvmInfo.append("ipv4Addr2", InetAddress.getByAddress(BigInteger.valueOf(info.ipv4Addr2).toByteArray()).getHostAddress());
						}
						if (info.ipv4Addr3 != 0) {
							jvmInfo.append("ipv4Addr3", InetAddress.getByAddress(BigInteger.valueOf(info.ipv4Addr3).toByteArray()).getHostAddress());
						}
						if (info.ipv4Addr4 != 0) {
							jvmInfo.append("ipv4Addr4", InetAddress.getByAddress(BigInteger.valueOf(info.ipv4Addr4).toByteArray()).getHostAddress());
						}

						jvmInfo.append("pid", info.pid)
							.append("loadedClassCount", info.loadedClassCount)
							.append("heapCommited", info.heapCommited)
							.append("heapInit", info.heapInit)
							.append("heapMax", info.heapMax)
							.append("heapUsed", info.heapUsed)
							.append("processCpuLoad", info.processCpuLoad)
							.append("systemCpuLoad", info.systemCpuLoad)
							.append("freePhysMem", info.freePhysMem)
							.append("freeSwap", info.freeSwap)
							.append("totalPhysMem", info.totalPhysMem)
							.append("totalSwap", info.totalSwap)
							.append("threadCount", info.threadCount)
							.append("threadDaemonCount", info.threadDaemonCount)
							.append("peakThreadCount", info.peakThreadCount)
							.append("totalStartedThreadCount", info.totalStartedThreadCount)
							.append("lastTimeGc1", info.lastTimeGc1)
							.append("lastTimeGc2", info.lastTimeGc2)
							.append("totalCountGc1", info.totalCountGc1)
							.append("totalCountGc2", info.totalCountGc2)
							.append("time", info.time)
							.append("timestamp", info.timestamp);

						ops.insert(jvmInfo);
					}

					ops.execute();
					index += count;
					buffer.clear();
				}

				LOG.info("Loaded " + index + " records of jvm monitoring");

			}
		} catch (IOException e) {
			LOG.severe("Error loading jvm monitoring " + e.getMessage());
		}
	}
}
