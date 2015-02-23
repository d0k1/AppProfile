package com.focusit.agent.analyzer.data.netty.os;

import com.focusit.agent.analyzer.configuration.MongoConfiguration;
import com.focusit.agent.analyzer.data.netty.DataBuffer;
import com.focusit.agent.analyzer.data.netty.DataImport;
import com.focusit.agent.metrics.samples.OSInfo;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Denis V. Kirpichenkov on 23.02.15.
 */
@Component
public class OSDataImport extends DataImport<OSInfo> {

	@Named(MongoConfiguration.OS_COLLECTION)
	@Inject
	DBCollection collection;

	@Override
	protected void importSampleInt(long appId, long sessionId, long recId, OSInfo sample, DataBuffer buffer) {
		if (!isMonitoringEnabled(appId)){
			return;
		}

		BasicDBObject osInfo = new BasicDBObject("appId", appId).append("sessionId", sessionId).append("recId", recId);

		BasicDBList ifIn = new BasicDBList();
		BasicDBList ifOut = new BasicDBList();
		BasicDBList reads = new BasicDBList();
		BasicDBList writes = new BasicDBList();

		for(int i=0;i<OSInfo.DEVICES;i++){
			ifIn.add(sample.ifIn[i]);
			ifOut.add(sample.ifOut[i]);
			reads.add(sample.reads[i]);
			writes.add(sample.writes[i]);
		}

		osInfo.append("ifIn", ifIn).append("ifOut", ifOut).append("reads", reads).append("writes", writes);
		osInfo.append("time", sample.time).append("timestamp", sample.timestamp);

		if(buffer!=null && buffer.getCapacity()>0) {
			buffer.holdDbItem(osInfo);
		} else {
			collection.insert(osInfo);
		}
	}
}
