package com.focusit.agent.mongo;

import com.focusit.agent.metrics.samples.ExecutionInfo;
import com.mongodb.BasicDBObject;
import com.mongodb.BulkWriteOperation;
import com.mongodb.DB;
import com.mongodb.DBCollection;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
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

		int samples = 4000000;
		ByteBuffer buffer = ByteBuffer.allocate(samples*ExecutionInfo.sizeOf());

		try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {

			try(FileChannel ch = raf.getChannel()){
				int content;
				long index = 0;

				while ((content = ch.read(buffer)) != -1) {
					if (content < ExecutionInfo.sizeOf()) {
						throw new IOException("Error reading JvmInfo");
					}

					int count = content / ExecutionInfo.sizeOf();

					ExecutionInfo info = new ExecutionInfo();

					buffer.flip();
					BulkWriteOperation ops = collection.initializeUnorderedBulkOperation();
					for (int i = 0; i < count; i++) {

						info.readFromBuffer(buffer);

						BasicDBObject executionInfo = new BasicDBObject("sessionId", sessionId)
							.append("sample", index+i)
							.append("threadId", info.threadId)
							.append("eventId", info.eventId)
							.append("time", info.time)
							.append("method", info.method);

						ops.insert(executionInfo);
					}

					ops.execute();
					index += count;
					buffer.clear();

					LOG.info("Inserted " + index + " records of profiling statistics");
				}

				LOG.info("Loaded " + index + " records of profiling statistics");
			}


		} catch (IOException e) {
			LOG.severe("Error loading jvm monitoring " + e.getMessage());
		}

	}
}
