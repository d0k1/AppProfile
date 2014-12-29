package com.focusit.agent.analyzer.data.netty.jvm;

import com.focusit.agent.analyzer.data.netty.DataImport;
import com.focusit.agent.metrics.samples.JvmInfo;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * Created by Denis V. Kirpichenkov on 30.12.14.
 */
public class JvmDataImport extends DataImport<JvmInfo> {
	private static final String COLLECTION_NAME = "jvmmonitoring";
	DBCollection collection;

	public JvmDataImport(DB db) {
		super(db);
		collection = getCollection(COLLECTION_NAME);
	}

	@Override
	protected void importSampleInt(long appId, long sessionId, JvmInfo info) {
		BasicDBObject jvmInfo = new BasicDBObject("appId", appId).append("sessionId", sessionId);

		try {
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
		} catch (UnknownHostException e) {
			e.printStackTrace();
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

		collection.insert(jvmInfo);
	}
}
