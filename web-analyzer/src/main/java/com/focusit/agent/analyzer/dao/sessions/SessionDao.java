package com.focusit.agent.analyzer.dao.sessions;

import com.focusit.agent.analyzer.configuration.MongoConfiguration;
import com.focusit.agent.analyzer.data.sessions.AppInfo;
import com.focusit.agent.analyzer.data.sessions.SessionInfo;
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
public class SessionDao {

	@Inject
	@Named(MongoConfiguration.SESSIONS_COLLECTION)
	DBCollection sessions;

	@Inject
	@Named(MongoConfiguration.JVM_COLLECTION)
	DBCollection jvm;

	@Inject
	@Named(MongoConfiguration.METHODSMAP_COLLECTION)
	DBCollection methods;

	@Inject
	@Named(MongoConfiguration.STATISTICS_COLLECTION)
	DBCollection statistics;

	public Collection<AppInfo> getAppIds(){
		Collection<AppInfo> result = new ArrayList<>();

		try(DBCursor cursor = sessions.find()){
			while(cursor.hasNext()){
				DBObject session = cursor.next();
				result.add(new AppInfo((Long)session.get("appId"),(Long)session.get("appId")));
			}
		}
		return result;
	}

	public Collection<SessionInfo> getSessions(long appId){
		Collection<SessionInfo> result = new ArrayList<>();

		BasicDBObject query = new BasicDBObject("appId", appId);
		try(DBCursor cursor = sessions.find(query)){
			while(cursor.hasNext()){
				DBObject session = cursor.next();

				BasicDBObject countQuery = new BasicDBObject("appId", appId).append("sessionId", session.get("sessionId"));
				long jvmCount = jvm.find(countQuery).count();
				long methodsCount = jvm.find(countQuery).count();
				long statisticsCount = jvm.find(countQuery).count();
				result.add(new SessionInfo((Long)session.get("sessionId"), jvmCount, statisticsCount, methodsCount, (Long)session.get("date")));
			}
		}
		return result;
	}
}
