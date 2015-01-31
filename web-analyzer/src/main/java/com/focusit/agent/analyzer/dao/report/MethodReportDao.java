package com.focusit.agent.analyzer.dao.report;

import com.focusit.agent.analyzer.configuration.MongoConfiguration;
import com.focusit.agent.analyzer.data.statistics.MethodReportSample;
import com.mongodb.*;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by Denis V. Kirpichenkov on 01.02.15.
 */
@Repository
public class MethodReportDao {
	@Named(MongoConfiguration.REPORT_COLLECTION)
	@Inject
	DBCollection reports;

	@Named(MongoConfiguration.METHODSMAP_COLLECTION)
	@Inject
	DBCollection methodmaps;

	@Inject
	DB db;

	public boolean analyzeReport(long appId, long sessionId, long recId) {
		HashMap<Long, String> methods = new HashMap<>();
		BasicDBObject query = new BasicDBObject("appId", appId).append("sessionId", sessionId);
		BasicDBObject methodQuery = new BasicDBObject("appId", appId).append("sessionId", sessionId);

		if (recId > -1) {
			query.append("recId", recId);
		}

		try (DBCursor cursor = reports.find(query, new BasicDBObject("methodId", 1))) {
			while (cursor.hasNext()) {
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

				reports.update(new BasicDBObject("_id", item.get("_id")), new BasicDBObject("$set", new BasicDBObject("methodName", methodName)));
			}
		}
		return true;
	}

	public Collection<MethodReportSample> getMethodsReport(long appId, long sessionId, long recId) {
		Collection<MethodReportSample> result = new ArrayList<>();

		DBCursor cursor = reports.find(new BasicDBObject("appId", appId).append("sessionId", sessionId).append("recId", recId)).sort(new BasicDBObject("callCount", -1).append("minTime", -1));

		while (cursor.hasNext()) {
			BasicDBObject item = (BasicDBObject) cursor.next();
			MethodReportSample sample = new MethodReportSample(appId, sessionId, recId, (Long) item.get("methodId"), (String) item.get("methodName"), (Long) item.get("callCount"), (Long) item.get("minTime"), (Long) item.get("maxTime"), (Long)item.get("totalTime"));

			result.add(sample);
		}

		return result;
	}
}
