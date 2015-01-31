package com.focusit.agent.analyzer.data.netty;

import com.focusit.agent.analyzer.data.statistics.MethodCallSample;
import com.focusit.agent.analyzer.data.statistics.MethodReportSample;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Data buffer to hold method report data
 * Created by Denis V. Kirpichenkov on 26.01.15.
 */
public class ReportDataBuffer extends DataBuffer {
	private final static Logger LOG = LoggerFactory.getLogger(ReportDataBuffer.class);
	// appId                                                        //sessionId            // recId                 // methodId
	private final static ConcurrentHashMap<Long, ConcurrentHashMap<Long, ConcurrentHashMap<Long, ConcurrentHashMap<Long, MethodReportSample>>>> data = new ConcurrentHashMap<>();

	// appId                                                        //sessionId            //recId
	private final static ConcurrentHashMap<Long, ConcurrentHashMap<Long, ConcurrentHashMap<Long, AtomicInteger>>> counts = new ConcurrentHashMap<>();

	public ReportDataBuffer(int size, DBCollection collection, String name) {
		super(size, collection, name);
	}

	public void updateReport(long appId, long sessionId, long recId, long methodId, long time) {
		if (data.get(appId) == null) {
			data.put(appId, new ConcurrentHashMap<Long, ConcurrentHashMap<Long, ConcurrentHashMap<Long, MethodReportSample>>>());
		}

		if (data.get(appId).get(sessionId) == null) {
			data.get(appId).put(sessionId, new ConcurrentHashMap<Long, ConcurrentHashMap<Long, MethodReportSample>>());
		}

		if (data.get(appId).get(sessionId).get(recId) == null) {
			data.get(appId).get(sessionId).put(recId, new ConcurrentHashMap<Long, MethodReportSample>());
		}

		if (data.get(appId).get(sessionId).get(recId).get(methodId) == null) {
			data.get(appId).get(sessionId).get(recId).put(methodId, new MethodReportSample(appId, sessionId, recId, methodId, null));
		}

		data.get(appId).get(sessionId).get(recId).get(methodId).addCallCount(time);
	}

	public AtomicInteger getCounter(long appId, long sessionId, long recId) {
		if (counts.get(appId) == null) {
			counts.put(appId, new ConcurrentHashMap<Long, ConcurrentHashMap<Long, AtomicInteger>>());
		}

		if (counts.get(appId).get(sessionId) == null) {
			counts.get(appId).put(sessionId, new ConcurrentHashMap<Long, AtomicInteger>());
		}

		if (counts.get(appId).get(sessionId).get(recId) == null) {
			counts.get(appId).get(sessionId).put(recId, new AtomicInteger());
		}

		return counts.get(appId).get(sessionId).get(recId);
	}

	private Set<Map.Entry<Long, MethodReportSample>> getMethodReport(long appId, long sessionId, long recId) {
		return data.get(appId).get(sessionId).get(recId).entrySet();
	}

	private void clearMethodReport(long appId, long sessionId, long recId) {
		data.get(appId).get(sessionId).get(recId).clear();
	}

	@Override
	public void holdItem(long appId, long sessionId, long recId, Object object) {
		try {
			workLock.lock();

			LOG.debug("Holding item in {} count=" + count.get(), name);

			getCounter(appId, sessionId, recId).incrementAndGet();
			MethodCallSample sample = (MethodCallSample) object;
			updateReport(appId, sessionId, recId, sample.methodId, sample.finishTime - sample.startTime);

//			if (count.incrementAndGet() == capacity) {
//				flushBuffer();
//			}

		} finally {
			workLock.unlock();
		}
		super.holdItem(appId, sessionId, recId, object);
	}

	@Override
	public void flushBuffer() {
		workLock.lock();
		try {
			Enumeration<Long> apps = data.keys();
			while(apps.hasMoreElements()){
				long appId = apps.nextElement();
				Enumeration<Long> sessions = data.get(appId).keys();
				while(sessions.hasMoreElements()){
					long sessionId = sessions.nextElement();

					Enumeration<Long> recs = data.get(appId).get(sessionId).keys();
					while(recs.hasMoreElements()){
						long recId = recs.nextElement();

						flushBuffer(appId, sessionId, recId);
					}
				}
			}
		} finally {
			workLock.unlock();
		}
	}

	@Override
	public void flushBuffer(long appId, long sessionId, long recId) {
		workLock.lock();
		try {

			if (getCounter(appId, sessionId, recId).get() == 0)
				return;

			Set<Map.Entry<Long, MethodReportSample>> reports = getMethodReport(appId, sessionId, recId);
			BasicDBObject objects[] = new BasicDBObject[reports.size()];
			int i = 0;
			for (Map.Entry<Long, MethodReportSample> report : getMethodReport(appId, sessionId, recId)) {
				objects[i] = new BasicDBObject("appId", appId).append("sessionId", sessionId).append("recId", recId).append("methodId", report.getKey()).append("callCount", report.getValue().getCallCount()).append("minTime", report.getValue().getMinTime()).append("maxTime", report.getValue().getMaxTime()).append("totalTime", report.getValue().getTotalTime());
				i++;
			}

			collection.insert(objects);
			LOG.debug("Flushing {} count=" + getCounter(appId, sessionId, recId).get(), name);

			getCounter(appId, sessionId, recId).set(0);
			clearMethodReport(appId, sessionId, recId);

		} finally {
			workLock.unlock();
		}
	}
}
