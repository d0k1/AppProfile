package com.focusit.agent.analyzer.data.netty.methodmap;

import com.focusit.agent.analyzer.configuration.MongoConfiguration;
import com.focusit.agent.analyzer.data.netty.DataBuffer;
import com.focusit.agent.analyzer.data.netty.DataImport;
import com.focusit.agent.metrics.dump.netty.MethodsMapNettyDumper;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Denis V. Kirpichenkov on 30.12.14.
 */
@Component
public class MethodMapImport extends DataImport<MethodsMapNettyDumper.MethodsMapSample> {

	@Named(MongoConfiguration.METHODSMAP_COLLECTION)
	@Inject
	DBCollection collection;

	@Override
	protected void importSampleInt(long appId, long sessionId, long recId, MethodsMapNettyDumper.MethodsMapSample sample, DataBuffer buffer) {
		BasicDBObject methodInfo = new BasicDBObject("appId", appId).append("sessionId", sessionId)
			.append("index", sample.index).append("method", sample.method);

		if(buffer!=null && buffer.getCapacity()>0) {
			buffer.holdItem(methodInfo);
		} else {
			collection.insert(methodInfo);
		}
	}

	@PostConstruct
	public void init(){
		initBuffer(1000, collection, "methods");
	}
}
