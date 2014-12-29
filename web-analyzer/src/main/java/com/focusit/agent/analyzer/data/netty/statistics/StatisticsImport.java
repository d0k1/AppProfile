package com.focusit.agent.analyzer.data.netty.statistics;

import com.focusit.agent.analyzer.data.netty.DataImport;
import com.focusit.agent.metrics.samples.ExecutionInfo;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

/**
 * Created by Denis V. Kirpichenkov on 30.12.14.
 */
public class StatisticsImport extends DataImport<ExecutionInfo> {
	private static final String COLLECTION_NAME = "statistics";
	DBCollection collection;

	public StatisticsImport(DB db) {
		super(db);
		collection = getCollection(COLLECTION_NAME);
	}

	@Override
	protected void importSampleInt(long appId, long sessionId, ExecutionInfo info) {
		BasicDBObject executionInfo = new BasicDBObject("appId", appId).append("sessionId", sessionId)
			.append("threadId", info.threadId)
			.append("eventId", info.eventId)
			.append("method", info.method)
			.append("time", info.time)
			.append("timestamp", info.timestamp);

		collection.insert(executionInfo);
	}

}
