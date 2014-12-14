package com.focusit.agent.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;

import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Main class to profiling data mongo writer
 * <p/>
 * Created by Denis V. Kirpichenkov on 14.12.14.
 */
public class Main {

	private static final Logger LOG = Logger.getLogger(Main.class.getName());

	private static final MongoLoader loaders[] = {new JvmMonitoringLoader(), new MethodMapLoader(), new StatisticsLoader()};

	public static void main(String[] args) throws UnknownHostException {
		LOG.info("Moving data to mongo");

		MongoClient client = new MongoClient(MongoConfiguration.getHost(), Integer.parseInt(MongoConfiguration.getPort()));
		DB bondDb = client.getDB(MongoConfiguration.getDbname());

		DBCollection sessions = bondDb.getCollection(MongoConfiguration.getSessionCollection());

		long nextSession = sessions.getCount() + 1;

		BasicDBObject sessionInfo = new BasicDBObject("sessionId", nextSession)
			.append("date", new Date().getTime());

		sessions.insert(sessionInfo);

		for (MongoLoader loader : loaders) {
			loader.loadData(bondDb, nextSession);
		}
	}
}
