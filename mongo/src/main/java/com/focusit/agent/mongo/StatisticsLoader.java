package com.focusit.agent.mongo;

import com.focusit.agent.metrics.samples.ExecutionInfo;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * Class to load dumped profiler statistics into mongo collection
 * Created by Denis V. Kirpichenkov on 14.12.14.
 */
public class StatisticsLoader implements MongoLoader {
	private final static Logger LOG = Logger.getLogger(StatisticsLoader.class.getName());

	@Override
	public void loadData(DB bondDb, long sessionId) {
		String file = MongoConfiguration.getStatisticsFile();
		DBCollection collection = bondDb.getCollection(MongoConfiguration.getStatisticsCollection());

		try (FileInputStream fis = new FileInputStream(file)) {
			ByteBuffer buffer = ByteBuffer.allocate(ExecutionInfo.sizeOf());

			int content;
			long index = 0;
			while ((content = fis.read(buffer.array())) != -1) {
				if (content < ExecutionInfo.sizeOf()) {
					throw new IOException("Error reading JvmInfo");
				}

				ExecutionInfo info = new ExecutionInfo();
				info.readFromLongBuffer(buffer.asLongBuffer());

				BasicDBObject executionInfo = new BasicDBObject("sessionId", sessionId)
					.append("threadId", info.threadId)
					.append("start", info.start)
					.append("end", info.end)
					.append("method", info.method);

				collection.insert(executionInfo);
				index++;
			}

			LOG.info("Loaded " + index + " records of profiling statistics");

		} catch (IOException e) {
			LOG.severe("Error loading jvm monitoring " + e.getMessage());
		}

	}
}
