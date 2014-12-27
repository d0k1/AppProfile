package com.focusit.agent.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

/**
 * Move to netty profiling method map
 * Created by Denis V. Kirpichenkov on 14.12.14.
 */
public class MethodMapLoader implements MongoLoader {

	private final static Logger LOG = Logger.getLogger(MethodMapLoader.class.getName());

	@Override
	public void loadData(DB bondDb, long sessionId) {

		String file = MongoConfiguration.getMethodMapFile();
		DBCollection collection = bondDb.getCollection(MongoConfiguration.getMethodsMapCollection());

		StringBuilder buffer = new StringBuilder();

		BulkWriteOperation ops = collection.initializeUnorderedBulkOperation();

		try (FileInputStream fis = new FileInputStream(file)) {
			char buf[] = new char[1024];

			int content;
			long index = 0;
			try (InputStreamReader reader = new InputStreamReader(fis, "UTF-8")) {
				while ((content = reader.read(buf)) != -1) {
					for (int i = 0; i < content; i++) {
						if (buf[i] != 0) {
							buffer.append(buf[i]);
						} else {
							String method = buffer.toString();
							BasicDBObject methodInfo = new BasicDBObject("sessionId", sessionId)
								.append("index", index++).append("method", method);

							ops.insert(methodInfo);

							buffer.setLength(0);
						}
					}
				}
			}
			String method = buffer.toString();
			BasicDBObject methodInfo = new BasicDBObject("sessionId", sessionId)
				.append("index", index++).append("method", method);

			ops.insert(methodInfo);

			ops.execute();

			LOG.info("Loaded " + index + " records of methods map");

		} catch (IOException e) {
			LOG.severe("Error loading methodsmaps " + e.getMessage());
		}

	}
}
