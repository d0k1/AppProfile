package com.focusit.agent.analyzer.dao.statistics;

import com.focusit.agent.analyzer.configuration.MongoConfiguration;
import com.focusit.agent.analyzer.data.statistics.MethodSample;
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
public class StatisticsDao {

	@Named(MongoConfiguration.STATISTICS_COLLECTION)
	@Inject
	DBCollection statistics;

	@Named(MongoConfiguration.METHODSMAP_COLLECTION)
	@Inject
	DBCollection methodmaps;

	public Collection<MethodSample> getMethods(long appId, long sessionId) {
		Collection<MethodSample> result = new ArrayList<>();

		BasicDBObject sort = new BasicDBObject("index", 1);
		BasicDBObject query = new BasicDBObject("appId", appId).append("sessionId", sessionId);

		try (DBCursor cursor = methodmaps.find(query)) {
			try (DBCursor sorted = cursor.sort(sort)) {
				while(sorted.hasNext()) {
					DBObject method = sorted.next();
					result.add(new MethodSample((Long) method.get("index"), (String) method.get("method")));
				}
			}
		}

		return result;
	}
}
