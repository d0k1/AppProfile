package com.focusit.agent.analyzer.dao.statistics;

import com.focusit.agent.analyzer.configuration.MongoConfiguration;
import com.focusit.agent.analyzer.data.statistics.MethodCallSample;
import com.mongodb.*;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Denis V. Kirpichenkov on 24.12.14.
 */
@Repository
public class StatisticsDao {

	@Named(MongoConfiguration.STATISTICS_COLLECTION)
	@Inject
	DBCollection statistics;

	@Named(MongoConfiguration.METHODSMAP_COLLECTION)
	@Inject
	DBCollection methodmaps;

	@Inject
	DB db;

	public Collection<MethodCallSample> getMethods(long appId, long sessionId, long recId) {
		Collection<MethodCallSample> result = new ArrayList<>();

		BasicDBObject query = new BasicDBObject("appId", appId).append("sessionId", sessionId);

		if(recId>-1) {
			query.append("recId", recId);
		}

		query.append("parents", new BasicDBObject("$size", 0));

		try (DBCursor cursor = statistics.find(query)) {
			while(cursor.hasNext()) {
				DBObject method = cursor.next();
				result.add(dbobject2MethodCall(method));
			}
		}

		return result;
	}

	private MethodCallSample dbobject2MethodCall(DBObject object){
		MethodCallSample sample = new MethodCallSample((String)object.get("_id"),(Long)object.get("threadId"), (Long)object.get("methodId"), (String)object.get("methodName"));
		sample.startTime = (Long)object.get("startTime");
		sample.finishTime = (Long)object.get("finishTime");
		sample.starttimestamp = (Long)object.get("starttimestamp");
		sample.finishtimestamp = (Long)object.get("finishtimestamp");
		BasicDBList parents = (BasicDBList)object.get("parents");

		for(Object parent:parents){
			sample.parents.add(parent.toString());
		}

		return sample;
	}

	public boolean analyzeSession(long appId, long sessionId, long recId){
		HashMap<Long, String> methods = new HashMap<>();
		BasicDBObject query = new BasicDBObject("appId", appId).append("sessionId", sessionId);
		BasicDBObject methodQuery = new BasicDBObject("appId", appId).append("sessionId", sessionId);

		if(recId>-1) {
			query.append("recId", recId);
		}

		try (DBCursor cursor = statistics.find(query, new BasicDBObject("methodId", 1))){
			while(cursor.hasNext()) {
				DBObject item = cursor.next();
				long methodId = (Long) item.get("methodId");
				String methodName = methods.get(methodId);
				if (methodName == null) {
					DBObject methodNameData = methodmaps.findOne(methodQuery.append("index", methodId), new BasicDBObject("method", 1));
					if (methodNameData != null) {
						methodName = (String) methodNameData.get("method");
						methods.put(methodId, methodName);
					}
				}

				statistics.update(new BasicDBObject("_id", item.get("_id")), new BasicDBObject("$set", new BasicDBObject("methodName", methodName)));
			}
		}
		return true;
	}

	public Collection<MethodCallSample> getMethodsByParents(long appId, long sessionId, long recId, String[] parents) {
		Collection<MethodCallSample> result = new ArrayList<>();

		BasicDBObject query = new BasicDBObject("appId", appId).append("sessionId", sessionId);

		if(recId>-1) {
			query.append("recId", recId);
		}

		query.append("parents", new BasicDBObject("$all", parents));

		try (DBCursor cursor = statistics.find(query)) {
			while(cursor.hasNext()) {
				DBObject method = cursor.next();
				result.add(dbobject2MethodCall(method));
			}
		}

		return result;
	}
}
