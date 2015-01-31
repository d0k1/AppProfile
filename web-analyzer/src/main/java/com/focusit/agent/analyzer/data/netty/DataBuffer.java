package com.focusit.agent.analyzer.data.netty;

import com.mongodb.BulkWriteOperation;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Buffer to bulk insert data to mongodb
 * Created by Denis V. Kirpichenkov on 17.01.15.
 */
public class DataBuffer {
	protected final DBCollection collection;
	protected final int capacity;
	protected AtomicInteger count = new AtomicInteger(0);
	protected BulkWriteOperation bulkWriteOperation;
	protected ReentrantLock workLock = new ReentrantLock();
	protected final String name;
	private final static Logger LOG = LoggerFactory.getLogger(DataBuffer.class);

	public DataBuffer(int size, DBCollection collection, String name){
		capacity = size;
		this.collection = collection;
		this.name = name;
		bulkWriteOperation = collection.initializeOrderedBulkOperation();
	}

	public void holdDbItem(DBObject object){
		try {
			workLock.lock();
			bulkWriteOperation.insert(object);
			LOG.debug("Holding item in {} count=" + count.get(), name);
			if (count.incrementAndGet() == capacity) {
				flushBuffer();
			}
		} finally {
			workLock.unlock();
		}
	}

	public void holdItem(long appId, long sessionId, long recId, Object object){

	}

	public void flushBuffer(){
		try {
			workLock.lock();

			if(count.get()==0)
				return;

			bulkWriteOperation.execute();
			bulkWriteOperation = collection.initializeOrderedBulkOperation();
			LOG.debug("Flushing {} count="+count.get(), name);

			count.set(0);
		} finally {
			workLock.unlock();
		}
	}

	public void flushBuffer(long appId, long sessionId, long recId){
		flushBuffer();
	}

	public int getCapacity(){
		return capacity;
	}
}
