package com.focusit.agent.analyzer.data.netty.statistics;

import com.focusit.agent.analyzer.configuration.MongoConfiguration;
import com.focusit.agent.analyzer.data.netty.DataImport;
import com.focusit.agent.analyzer.data.statistics.MethodCallSample;
import com.focusit.agent.metrics.samples.ExecutionInfo;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Denis V. Kirpichenkov on 30.12.14.
 */
@Component
public class StatisticsImport extends DataImport<ExecutionInfo> {
	private final static Logger LOG = LoggerFactory.getLogger(StatisticsImport.class);

	@Named(MongoConfiguration.STATISTICS_COLLECTION)
	@Inject
	DBCollection statistics;

	// appId // sessionId // threadId
	Map<Long, Map<Long, Map<Long, LinkedList<MethodCallSample>>>> callSites = new ConcurrentHashMap<>();

	@Override
	public void onSessionStart(long appId) {
		long sessionId = getSessionIdByAppId(appId);
		Map<Long, Map<Long, LinkedList<MethodCallSample>>> sessionIdCalls = callSites.get(appId);
		if(sessionIdCalls==null) {
			sessionIdCalls = new ConcurrentHashMap<>();
			callSites.put(appId, sessionIdCalls);
		}

		if(sessionId>1){
			if(sessionIdCalls.get(sessionId-1)!=null){
				sessionIdCalls.remove(sessionId-1);
			}
		}
		sessionIdCalls.put(sessionId, new ConcurrentHashMap<Long, LinkedList<MethodCallSample>>());
	}

	@Override
	protected void importSampleInt(long appId, long sessionId, long recId, ExecutionInfo info) {
		if(isProfilingEnabled(appId)) {
			// if session really started and internal state initialized
			if(callSites.get(appId).get(sessionId)!=null)
				processExecutionInfo(appId, sessionId, recId, info);
		}
	}

	private void processExecutionInfo(long appId, long sessionId, long recId, ExecutionInfo info){
		Map<Long, LinkedList<MethodCallSample>> map = callSites.get(appId).get(sessionId);
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
				LOG.warn("Method exit found before method entry {}", info.toString());
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
			insertMethodCallSample(statistics, sample, appId, sessionId, recId);
		} else if(event==0){
			MethodCallSample sample = new MethodCallSample(new ObjectId().toString(), threadId, methodId, null);
			sample.starttimestamp = timestamp;
			sample.startTime = time;

			Iterator<MethodCallSample> parents = samples.descendingIterator();
			while(parents.hasNext()) {
				MethodCallSample parent = parents.next();
				sample.parents.add(parent._id);
			}
			samples.add(sample);
		}

	}

	private void insertMethodCallSample(DBCollection collection, MethodCallSample sample, long appId, long sessionId, long recId) {
		BasicDBObject method = new BasicDBObject("_id", sample._id).append("threadId", sample.threadId).append("methodId", sample.methodId).append("methodName", sample.methodName)
			.append("startTime", sample.startTime).append("finishTime", sample.finishTime).append("starttimestamp", sample.starttimestamp)
			.append("finishtimestamp", sample.finishtimestamp).append("appId", appId).append("sessionId", sessionId).append("recId", recId);

		BasicDBList parents = new BasicDBList();

		for(String parent:sample.parents){
			parents.add(parent);
		}
		method.append("parents", parents);

		collection.insert(method);
	}
}
