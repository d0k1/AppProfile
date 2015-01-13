package com.focusit.agent.analyzer.dao.jvm;

import com.focusit.agent.analyzer.configuration.MongoConfiguration;
import com.focusit.agent.analyzer.data.jvm.CpuSample;
import com.focusit.agent.analyzer.data.jvm.HeapSample;
import com.focusit.agent.analyzer.data.jvm.JvmSamplesCounter;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Denis V. Kirpichenkov on 24.12.14.
 */
@Repository
public class JvmDao {

	@Named(MongoConfiguration.JVM_COLLECTION)
	@Inject
	DBCollection jvm;

	public Collection<HeapSample> getLastHeapData(long appId, long sessionId, long recId, long seconds){
		Collection<HeapSample> result = new ArrayList<>();

		BasicDBObject sort = new BasicDBObject("timestamp", -1);
		BasicDBObject query = new BasicDBObject("sessionId", sessionId).append("appId", appId);

		if(recId>-1) {
			query.append("recId", recId);
		}

		Long maxTimestamp = null;

		try(DBCursor cursor = jvm.find(query)){
			try(DBCursor sorted = cursor.sort(sort)) {
				while (sorted.hasNext()) {
					DBObject info = sorted.next();
					if(maxTimestamp!=null){
						if((Long) info.get("timestamp") < maxTimestamp - seconds*1000){
							break;
						}
					}
					if(maxTimestamp==null){
						maxTimestamp = (Long) info.get("timestamp");
					}
					result.add(new HeapSample((Long) info.get("heapInit"), (Long) info.get("heapUsed"), (Long) info.get("heapCommited"), (Long) info.get("heapMax"), (Long) info.get("timestamp")));
				}
			}
		}
		return result;
	}

	public Collection<CpuSample> getLastCpuData(long appId, long sessionId, long recId, long seconds){
		Collection<CpuSample> result = new ArrayList<>();

		BasicDBObject sort = new BasicDBObject("timestamp", -1);
		BasicDBObject query = new BasicDBObject("sessionId", sessionId).append("appId", appId);

		if(recId>-1) {
			query.append("recId", recId);
		}

		Long maxTimestamp = null;

		try(DBCursor cursor = jvm.find(query)){
			try(DBCursor sorted = cursor.sort(sort)) {
				while (sorted.hasNext()) {
					DBObject info = sorted.next();
					if(maxTimestamp!=null){
						if((Long) info.get("timestamp") < maxTimestamp - seconds*1000){
							break;
						}
					}
					if(maxTimestamp==null){
						maxTimestamp = (Long) info.get("timestamp");
					}
					result.add(new CpuSample((Double) info.get("processCpuLoad"), (Double) info.get("systemCpuLoad"), (Long) info.get("timestamp")));
				}
			}
		}
		return result;
	}

	public Collection<HeapSample> getHeapData(long appId, long sessionId, long recId, long timestapmMin, long timestapmMax){
		Collection<HeapSample> result = new ArrayList<>();

		BasicDBObject sort = new BasicDBObject("timestamp", -1);
		BasicDBObject query = new BasicDBObject("sessionId", sessionId).append("appId", appId);
		if(recId>-1) {
			query.append("recId", recId);
		}
		query.append("timestamp", new BasicDBObject("$lte", timestapmMax).append("$gte", timestapmMin));

		try(DBCursor cursor = jvm.find(query)){
			try(DBCursor sorted = cursor.sort(sort)) {
				while (sorted.hasNext()) {
					DBObject info = sorted.next();
					result.add(new HeapSample((Long) info.get("heapInit"), (Long) info.get("heapUsed"), (Long) info.get("heapCommited"), (Long) info.get("heapMax"), (Long) info.get("timestamp")));
				}
			}
		}
		return result;
	}

	public Collection<CpuSample> getCpuData(long appId, long sessionId, long recId, long timestapmMin, long timestapmMax){
		Collection<CpuSample> result = new ArrayList<>();

		BasicDBObject sort = new BasicDBObject("timestamp", -1);
		BasicDBObject query = new BasicDBObject("sessionId", sessionId).append("appId", appId);
		if(recId>-1) {
			query.append("recId", recId);
		}
		query.append("timestamp", new BasicDBObject("$lte", timestapmMax).append("$gte", timestapmMin));

		try(DBCursor cursor = jvm.find(query)){
			try(DBCursor sorted = cursor.sort(sort)) {
				while (sorted.hasNext()) {
					DBObject info = sorted.next();
					result.add(new CpuSample((Double) info.get("processCpuLoad"), (Double) info.get("systemCpuLoad"), (Long) info.get("timestamp")));
				}
			}
		}
		return result;
	}

	public JvmSamplesCounter getJvmSamplesCount(long appId, long sessionId, long recId){
		JvmSamplesCounter empty = new JvmSamplesCounter(appId, sessionId, recId, 0, 0, 0);

		long count;
		long min;
		long max;

		BasicDBObject projection = new BasicDBObject("timestamp", 1);
		BasicDBObject sort = new BasicDBObject("timestamp", -1);
		BasicDBObject query = new BasicDBObject("sessionId", sessionId).append("appId", appId);
		if(recId>-1) {
			query.append("recId", recId);
		}

		count = jvm.count(query);

		if(count==0) {
			return empty;
		}

		max = (Long) jvm.find(query, projection).sort(sort).limit(1).next().get("timestamp");
		sort.put("timestamp", 1);
		min = (Long) jvm.find(query, projection).sort(sort).limit(1).next().get("timestamp");

		return new JvmSamplesCounter(appId, sessionId, recId, count, min, max);
	}
}
