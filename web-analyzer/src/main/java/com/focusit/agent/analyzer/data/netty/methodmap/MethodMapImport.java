package com.focusit.agent.analyzer.data.netty.methodmap;

import com.focusit.agent.analyzer.data.netty.DataImport;
import com.focusit.agent.metrics.dump.netty.MethodsMapNettyDumper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

/**
 * Created by Denis V. Kirpichenkov on 30.12.14.
 */
public class MethodMapImport extends DataImport<MethodsMapNettyDumper.MethodsMapSample> {
	private static final String COLLECTION_NAME = "methodsmap";
	DBCollection collection;

	@Override
	public void startNewSession(long appId) {
		newSession(appId);
	}

	public MethodMapImport(DB db) {
		super(db);
		collection = getCollection(COLLECTION_NAME);
	}

	@Override
	protected void importSampleInt(long appId, long sessionId, MethodsMapNettyDumper.MethodsMapSample sample) {
		BasicDBObject methodInfo = new BasicDBObject("appId", appId).append("sessionId", sessionId)
			.append("index", sample.index).append("method", sample.method);
		collection.insert(methodInfo);
	}
}
