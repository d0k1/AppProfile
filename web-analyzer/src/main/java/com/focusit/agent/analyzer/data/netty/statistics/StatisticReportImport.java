package com.focusit.agent.analyzer.data.netty.statistics;

import com.focusit.agent.analyzer.configuration.MongoConfiguration;
import com.focusit.agent.analyzer.data.netty.DataBuffer;
import com.focusit.agent.analyzer.data.netty.DataImport;
import com.focusit.agent.analyzer.data.netty.ReportDataBuffer;
import com.focusit.agent.analyzer.data.statistics.MethodCallSample;
import com.focusit.agent.metrics.samples.ExecutionInfo;
import com.mongodb.DBCollection;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Denis V. Kirpichenkov on 25.01.15.
 */
@Component
public class StatisticReportImport extends DataImport<ExecutionInfo> {
	private final static Logger LOG = LoggerFactory.getLogger(StatisticReportImport.class);

	@Named(MongoConfiguration.REPORT_COLLECTION)
	@Inject
	DBCollection collection;

	// appId // sessionId // threadId
	Map<Long, Map<Long, Map<Integer, LinkedList<MethodCallSample>>>> callSites = new ConcurrentHashMap<>();

	@Override
	public void onSessionStart(long appId) {
		long sessionId = getSessionIdByAppId(appId);
		Map<Long, Map<Integer, LinkedList<MethodCallSample>>> sessionIdCalls = callSites.get(appId);
		if(sessionIdCalls==null) {
			sessionIdCalls = new ConcurrentHashMap<>();
			callSites.put(appId, sessionIdCalls);
		}

		if(sessionId>1){
			if(sessionIdCalls.get(sessionId-1)!=null){
				sessionIdCalls.remove(sessionId-1);
			}
		}
		sessionIdCalls.put(sessionId, new ConcurrentHashMap<Integer, LinkedList<MethodCallSample>>());
	}

	@Override
	protected void importSampleInt(long appId, long sessionId, long recId, ExecutionInfo info, DataBuffer buffer) {
		if(isProfilingEnabled(appId)) {
			// if session really started and internal state initialized
			if(callSites.get(appId).get(sessionId)!=null)
				processExecutionInfo(appId, sessionId, recId, info, buffer);
		}
	}

	private void processExecutionInfo(long appId, long sessionId, long recId, ExecutionInfo info, DataBuffer buffer){
		Map<Integer, LinkedList<MethodCallSample>> map = callSites.get(appId).get(sessionId);
		int threadId = (Integer) info.threadId;
		byte event = (Byte) info.eventId;
		long methodId = (Long) info.method;
		long time = (Long) info.time;

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
				sample.startTime = time;
				samples.add(sample);

				return;
			}
		}
		if(event==1 || event==2){
			MethodCallSample sample = samples.removeLast();
			sample.finishTime = time;
			insertMethodCallSample(collection, sample, appId, sessionId, recId, buffer);
		} else if(event==0){
			MethodCallSample sample = new MethodCallSample(new ObjectId().toString(), threadId, methodId, null);
			sample.startTime = time;

			Iterator<MethodCallSample> parents = samples.descendingIterator();
			while(parents.hasNext()) {
				MethodCallSample parent = parents.next();
				sample.parents.add(parent._id);
			}
			samples.add(sample);
		}

	}

	private void insertMethodCallSample(DBCollection collection, MethodCallSample sample, long appId, long sessionId, long recId, DataBuffer buffer) {
		if(buffer!=null && buffer.getCapacity()>0) {
			buffer.holdItem(appId, sessionId, recId, sample);
		}
	}

	protected void initBuffer(int capacity, DBCollection collection, String name){
		buffer = new ReportDataBuffer(capacity, collection, name);
	}

	@PostConstruct
	public void init(){
		initBuffer(2000000, collection, "report");
	}

}
