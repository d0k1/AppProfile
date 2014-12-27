package com.focusit.agent.analyzer.dao.jvm;

import com.focusit.agent.analyzer.configuration.MongoConfiguration;
import com.focusit.agent.analyzer.data.jvm.CpuSample;
import com.focusit.agent.analyzer.data.jvm.HeapSample;
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

	public Collection<HeapSample> getLastHeapData(long sessionId, long seconds){
		Collection<HeapSample> result = new ArrayList<>();

		BasicDBObject sort = new BasicDBObject("timestamp", -1);
		BasicDBObject query = new BasicDBObject("sessionId", sessionId);

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

	public Collection<CpuSample> getLastCpuData(long sessionId, long seconds){
		Collection<CpuSample> result = new ArrayList<>();

		BasicDBObject sort = new BasicDBObject("timestamp", -1);
		BasicDBObject query = new BasicDBObject("sessionId", sessionId);

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
}
