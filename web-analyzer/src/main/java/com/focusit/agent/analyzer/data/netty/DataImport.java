package com.focusit.agent.analyzer.data.netty;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Denis V. Kirpichenkov on 30.12.14.
 */
public abstract class DataImport<S> {
	private final DB db;
	private final static String SESSIONS_COLLECTION="sessions";
	private final static ConcurrentHashMap<Long, Long> sessionIds = new ConcurrentHashMap<>();
	private static final ReentrantLock lock = new ReentrantLock(true);

	public DataImport(DB db){
		this.db = db;
	}

	public DB getDb() {
		return db;
	}

	public DBCollection getCollection(String name){
		return db.getCollection(name);
	}

	protected final void newSession(long appId){
		try {
			sessionIds.remove(appId);
			lock.lock();
			getSessionIdByAppId(appId);
		} finally {
			lock.unlock();
		}
	}

	public void startNewSession(long appId){
	}

	protected final long  getSessionIdByAppId(long appId){
		if(sessionIds.get(appId)!=null)
			return sessionIds.get(appId);

		try{
			lock.lock();

			if(sessionIds.get(appId)!=null)
				return sessionIds.get(appId);

			DBCollection sessions = db.getCollection(SESSIONS_COLLECTION);
			BasicDBObject query = new BasicDBObject();
			query.append("appId", appId);

			DBCursor cursor = sessions.find(query);
			BasicDBObject session = new BasicDBObject();
			session.append("appId", appId);
			session.append("date", System.currentTimeMillis());

			if(cursor==null || cursor.count()==0){
				session.append("sessionId", 1L);
				sessions.insert(session);
				sessionIds.put(appId, 1L);
				System.out.println("Class: "+this.getClass().getName()+": appId: "+appId+"! sessionId: 1");
				return 1L;
			}

			BasicDBObject sort = new BasicDBObject("sessionId", -1);
			cursor = cursor.sort(sort).limit(1);
			long nextSessionId = ((Long)cursor.next().get("sessionId"))+1;
			session.append("sessionId", nextSessionId);
			sessions.insert(session);

			System.out.println("Class: "+this.getClass().getName()+": appId: "+appId+"; sessionId: "+nextSessionId);
			sessionIds.put(appId, nextSessionId);
			return nextSessionId;
		} finally {
			lock.unlock();
		}
	}

	public void importSample(long appId, S sample){
		importSampleInt(appId, getSessionIdByAppId(appId), sample);
	}

	protected abstract void importSampleInt(long appId, long sessionId, S sample);
}
