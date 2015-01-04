package com.focusit.agent.analyzer.dao.sessions;

import com.focusit.agent.analyzer.configuration.MongoConfiguration;
import com.focusit.agent.analyzer.data.sessions.AppInfo;
import com.focusit.agent.analyzer.data.sessions.RecordInfo;
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
	@Named(MongoConfiguration.RECORDS_COLLECTION)
	DBCollection records;

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
				AppInfo info = new AppInfo((Long)session.get("appId"),String.valueOf((Long)session.get("appId")));

				if(!result.contains(info)) {
					result.add(info);
				}
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
				long methodsCount = methods.find(countQuery).count();
				long statisticsCount = statistics.find(countQuery).count();
				long recordsCount = records.find(countQuery).count();
				result.add(new SessionInfo((Long)session.get("sessionId"), jvmCount, statisticsCount, methodsCount, recordsCount, (Long)session.get("date")));
			}
		}
		return result;
	}

	public Collection<RecordInfo> getRecords(long appId, long sessionId) {
		Collection<RecordInfo> result = new ArrayList<>();

		result.add(new RecordInfo(-1, "All records"));

		BasicDBObject query = new BasicDBObject("appId", appId).append("sessionId", sessionId);
		try(DBCursor cursor = records.find(query)){
			while(cursor.hasNext()){
				DBObject session = cursor.next();
				result.add(new RecordInfo((Long)session.get("recordId"), String.valueOf((Long)session.get("recordId"))));
			}
		}
		return result;
	}
}
