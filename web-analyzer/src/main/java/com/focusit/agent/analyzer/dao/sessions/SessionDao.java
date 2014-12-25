package com.focusit.agent.analyzer.dao.sessions;

import com.focusit.agent.analyzer.configuration.MongoConfiguration;
import com.focusit.agent.analyzer.data.sessions.SessionInfo;
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

	public Collection<SessionInfo> getSessions(){
		Collection<SessionInfo> result = new ArrayList<>();

		try(DBCursor cursor = sessions.find()){
			while(cursor.hasNext()){
				DBObject session = cursor.next();
				result.add(new SessionInfo((Long)session.get("sessionId"), 0, 0, (Long)session.get("date")));
			}
		}
		return result;
	}
}
