package com.focusit.agent.analyzer.data.netty.statistics;

import com.focusit.agent.analyzer.data.netty.DataImport;
import com.focusit.agent.analyzer.data.statistics.MethodCallSample;
import com.focusit.agent.metrics.samples.ExecutionInfo;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import org.bson.types.ObjectId;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Denis V. Kirpichenkov on 30.12.14.
 */
public class StatisticsImport extends DataImport<ExecutionInfo> {
	private static final String COLLECTION_NAME = "statistics";
	DBCollection collection;

	ConcurrentHashMap<Long, HashMap<Long, HashMap<Long, LinkedList<MethodCallSample>>>> callSites = new ConcurrentHashMap<>();

	public StatisticsImport(DB db) {
		super(db);
		collection = getCollection(COLLECTION_NAME);
	}

	@Override
	public void startNewSession(long appId) {
		long sessionId = getSessionIdByAppId(appId);
		HashMap<Long, HashMap<Long, LinkedList<MethodCallSample>>> sessionIdCalls = callSites.get(appId);
		if(sessionIdCalls==null) {
			sessionIdCalls = new HashMap<>();
			callSites.put(appId, sessionIdCalls);
		}

		if(sessionId>1){
			if(sessionIdCalls.get(sessionId-1)!=null){
				sessionIdCalls.remove(sessionId-1);
			}
		}
		sessionIdCalls.put(sessionId, new HashMap<Long, LinkedList<MethodCallSample>>());
	}

	@Override
	protected void importSampleInt(long appId, long sessionId, ExecutionInfo info) {
		processExecutionInfo(appId, sessionId, info);
	}

	private void processExecutionInfo(long appId, long sessionId, ExecutionInfo info){
		HashMap<Long, LinkedList<MethodCallSample>> map = callSites.get(appId).get(sessionId);

		long threadId = (Long) info.threadId;
		long event = (Long) info.eventId;
		long methodId = (Long) info.method;
		long time = (Long) info.time;
		long timestamp = (Long) info.timestamp;

		LinkedList<MethodCallSample> samples = map.get(threadId);
		if (samples == null || samples.size()==0) {
			if(samples==null) {
				samples = new LinkedList<>();
				map.put(threadId, samples);
			}
			if (event == 1) {
				System.err.println("Method exit found before method entry");
				return;
			} else {

				MethodCallSample sample = new MethodCallSample(new ObjectId().toString(), threadId, methodId, null);
				sample.starttimestamp = timestamp;
				sample.startTime = time;
				samples.add(sample);

				return;
			}
		}
		if(event==1){
			MethodCallSample sample = samples.removeLast();
			sample.finishTime = time;
			sample.finishtimestamp = timestamp;
			insertMethodCallSample(collection, sample, appId, sessionId);
		} else if(event==0){
			MethodCallSample parent = samples.getLast();
			MethodCallSample sample = new MethodCallSample(new ObjectId().toString(), threadId, methodId, null);
			sample.starttimestamp = timestamp;
			sample.startTime = time;
			sample.parents.add(parent._id);
			samples.add(sample);
		}

	}

	private void insertMethodCallSample(DBCollection collection, MethodCallSample sample, long appId, long sessionId) {
		BasicDBObject method = new BasicDBObject("_id", sample._id).append("threadId", sample.threadId).append("methodId", sample.methodId).append("methodName", sample.methodName)
			.append("startTime", sample.startTime).append("finishTime", sample.finishTime).append("starttimestamp", sample.starttimestamp)
			.append("finishtimestamp", sample.finishtimestamp).append("appId", appId).append("sessionId", sessionId);

		String parents[] = new String[sample.parents.size()];
		int i = 0;
		for(String parent:sample.parents){
			parents[i] = parent;
		}
		method.append("parents", parents);

		collection.insert(method);
	}
}
