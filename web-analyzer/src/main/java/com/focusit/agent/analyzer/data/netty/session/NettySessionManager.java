package com.focusit.agent.analyzer.data.netty.session;

import com.focusit.agent.analyzer.configuration.MongoConfiguration;
import com.focusit.agent.analyzer.data.netty.DataImport;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Profiling session management
 * Created by Denis V. Kirpichenkov on 04.01.15.
 */
@Component
public class NettySessionManager {
	@Inject
	private DB db;
	private final String SESSIONS_COLLECTION= MongoConfiguration.SESSIONS_COLLECTION;
	private final String RECORDS_COLLECTION= MongoConfiguration.RECORDS_COLLECTION;
	// Map appId - current sessionId
	private final ConcurrentHashMap<Long, Long> sessionIds = new ConcurrentHashMap<>();
	// Map appId - current sessionId - current recordingId in current session of current appId
	private final Map<Long, Map<Long, Long>> recordings = new ConcurrentHashMap<>();
	// Map appId - boolean if storing monitoring data enabled. if false no data is written to mongodb.
	private final Map<Long, Boolean> enabledMonitoring = new ConcurrentHashMap<>();
	// Map appId - boolean if storing profiling data enabled. if false no data is written to mongodb.
	private final Map<Long, Boolean> enabledProfiling = new ConcurrentHashMap<>();
	// lock to synchronize work with appIds, sessionId, recordingIds and enabled/disbaled flag
	private final ReentrantLock lock = new ReentrantLock(true);

	private final List<Long> activeApps = new ArrayList<>();

	private boolean automonitoring = true;
	private boolean autoprofiling = true;

	private DataImport[] importToNotify;

	public void setAutomonitoring(boolean automonitoring) {
		this.automonitoring = automonitoring;
	}

	public void setAutoprofiling(boolean autoprofiling) {
		this.autoprofiling = autoprofiling;
	}

	public boolean isAutomonitoring() {
		return automonitoring;
	}

	public boolean isAutoprofiling() {
		return autoprofiling;
	}

	public void setImportsToNotify(DataImport...imports) {
		importToNotify = imports;
	}

	public void startRecording(long appId, long sessionId) {
		try{
			lock.lock();
			Map<Long, Long> sessionIdRecs = recordings.get(appId);
			if(sessionIdRecs==null){
				sessionIdRecs = new ConcurrentHashMap<>();
				recordings.put(appId, sessionIdRecs);
			}

			Long recId = sessionIdRecs.get(sessionId);
			DBCollection records = db.getCollection(RECORDS_COLLECTION);

			BasicDBObject record = new BasicDBObject();
			record.append("appId", appId);
			record.append("sessionId", sessionId);
			record.append("date", System.currentTimeMillis());

			if(recId==null) {
				sessionIdRecs.put(sessionId, 0L);
				record.append("recordId", 0L);
			} else {
				sessionIdRecs.put(sessionId, recId + 1);
				record.append("recordId", recId + 1);
			}

			records.insert(record);
		} finally {
			lock.unlock();
		}
	}

	private final void startSession(long appId){
		try {
			sessionIds.remove(appId);
			lock.lock();

			if(!activeApps.contains(appId)){
				activeApps.add(appId);
			}

			if(enabledMonitoring.get(appId)==null){
				enabledMonitoring.put(appId, automonitoring);
			}

			if(enabledProfiling.get(appId)==null){
				enabledProfiling.put(appId, autoprofiling);
			}

			long sessionId = getSessionIdByAppId(appId);
			startRecording(appId, sessionId);
		} finally {
			lock.unlock();
		}
	}

	private void stopSession(long appId){
		try {
			if(activeApps.contains(appId)) {
				activeApps.remove(appId);
			}
		} finally {
			lock.unlock();
		}
	}

	public boolean isMonitoringEnabled(long appId){
		return enabledMonitoring.get(appId);
	}

	public boolean isProfilingEnabled(long appId){
		return enabledProfiling.get(appId);
	}

	public void setMonitoringEnabled(long appId, boolean value){
		try {
			lock.lock();
			enabledMonitoring.put(appId, value);
		}finally {
			lock.unlock();
		}
	}

	public void setProfilingEnabled(long appId, boolean value){
		try {
			lock.lock();
			enabledMonitoring.put(appId, value);
		}finally {
			lock.unlock();
		}
	}

	protected void onSesionStart(long appId){
		startSession(appId);
		for(DataImport dataImport:importToNotify)
			dataImport.onSessionStart(appId);
	}

	protected void onSessionStop(long appId){
		stopSession(appId);
		for(DataImport dataImport:importToNotify)
			dataImport.onSessionStop(appId);
	}

	public final long getRecIdBySessionIdByAppId(long appId, long sessionId){
		return recordings.get(appId).get(sessionId);
	}

	public final long  getSessionIdByAppId(long appId){
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
}
