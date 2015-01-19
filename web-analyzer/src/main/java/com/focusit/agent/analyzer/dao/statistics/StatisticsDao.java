package com.focusit.agent.analyzer.dao.statistics;

import com.focusit.agent.analyzer.configuration.MongoConfiguration;
import com.focusit.agent.analyzer.data.statistics.MethodCallSample;
import com.focusit.agent.analyzer.data.statistics.MethodStatSample;
import com.mongodb.*;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Denis V. Kirpichenkov on 24.12.14.
 */
@Repository
public class StatisticsDao {

	@Named(MongoConfiguration.STATISTICS_COLLECTION)
	@Inject
	DBCollection statistics;

	@Named(MongoConfiguration.METHODSMAP_COLLECTION)
	@Inject
	DBCollection methodmaps;

	@Inject
	DB db;

	public Collection<MethodCallSample> getMethods(long appId, long sessionId, long recId) {
		Collection<MethodCallSample> result = new ArrayList<>();

		BasicDBObject query = new BasicDBObject("appId", appId).append("sessionId", sessionId);

		if(recId>-1) {
			query.append("recId", recId);
		}

		query.append("parents", new BasicDBObject("$size", 0));

		try (DBCursor cursor = statistics.find(query)) {
			while(cursor.hasNext()) {
				DBObject method = cursor.next();
				result.add(dbobject2MethodCall(method));
			}
		}

		return result;
	}

	private MethodCallSample dbobject2MethodCall(DBObject object){
		MethodCallSample sample = new MethodCallSample((String)object.get("_id"),(Long)object.get("threadId"), (Long)object.get("methodId"), (String)object.get("methodName"));
		sample.startTime = (Long)object.get("startTime");
		sample.finishTime = (Long)object.get("finishTime");
		sample.starttimestamp = (Long)object.get("starttimestamp");
		sample.finishtimestamp = (Long)object.get("finishtimestamp");
		BasicDBList parents = (BasicDBList)object.get("parents");

		for(Object parent:parents){
			sample.parents.add(parent.toString());
		}

		return sample;
	}

	public boolean analyzeSession(long appId, long sessionId, long recId){
		HashMap<Long, String> methods = new HashMap<>();
		BasicDBObject query = new BasicDBObject("appId", appId).append("sessionId", sessionId);
		BasicDBObject methodQuery = new BasicDBObject("appId", appId).append("sessionId", sessionId);

		if(recId>-1) {
			query.append("recId", recId);
		}

		try (DBCursor cursor = statistics.find(query, new BasicDBObject("methodId", 1))){
			while(cursor.hasNext()) {
				DBObject item = cursor.next();
				long methodId = (Long) item.get("methodId");
				String methodName = methods.get(methodId);
				if (methodName == null) {
					DBObject methodNameData = methodmaps.findOne(methodQuery.append("index", methodId), new BasicDBObject("method", 1));
					if (methodNameData != null) {
						methodName = (String) methodNameData.get("method");
						methods.put(methodId, methodName);
					}
				}

				statistics.update(new BasicDBObject("_id", item.get("_id")), new BasicDBObject("$set", new BasicDBObject("methodName", methodName)));
			}
		}
		return true;
	}

	public Collection<MethodCallSample> getMethodsByParents(long appId, long sessionId, long recId, String[] parents) {
		Collection<MethodCallSample> result = new ArrayList<>();

		BasicDBObject query = new BasicDBObject("appId", appId).append("sessionId", sessionId);

		if(recId>-1) {
			query.append("recId", recId);
		}

		BasicDBList list = new BasicDBList();
		for(String parent:parents) {
			list.add(parent);
		}
		query.append("parents", new BasicDBObject("$all", list).append("$size", list.size()));

		try (DBCursor cursor = statistics.find(query)) {
			while(cursor.hasNext()) {
				DBObject method = cursor.next();
				result.add(dbobject2MethodCall(method));
			}
		}

		return result;
	}

	public Collection<MethodStatSample> getMethodsStat(long appId, long sessionId, long recId){

		String map="function(){\n" +
			"    var nanos = this.finishTime-this.startTime;\n" +
			"    var threads = [0.0+this.threadId];\n" +
			"    var sumTime = 0.0+nanos;\n" +
			"    var minNanos = 0.0+nanos;\n" +
			"    var maxNanos = 0.0+nanos;\n" +
			"    var times = [0.0+nanos];\n" +
			"    \n" +
			"    emit(this.methodName, {\"count\": 1, \"threadId\":this.threadId, \"nanos\":nanos, \"threads\":threads, \"times\":times, \"minNanos\":minNanos, \"maxNanos\":maxNanos, \"totalTime\":sumTime});\n" +
			"};\n";

		String reduce="function(key, values){\n" +
			"    var count = 0;\n" +
			"    var threads = [];\n" +
			"    var sumTime = -1;\n" +
			"    var minNanos = -1;\n" +
			"    var maxNanos = -1;\n" +
			"    var times = [];\n" +
			"    \n" +
			"    for(var i=0;i<values.length;i++){\n" +
			"        count += 1;\n" +
			"                    \n" +
			"        if(threads.indexOf(Number(values[i].threadId))<0)\n" +
			"            threads.push(Number(values[i].threadId));\n" +
			"        \n" +
			"        sumTime = sumTime+values[i].nanos;\n" +
			"        times.push(values[i].nanos);\n" +
			"\n" +
			"        if(minNanos==-1){\n" +
			"            minNanos = values[i].nanos;\n" +
			"            if(maxNanos==-1)\n" +
			"                maxNanos = values[i].nanos;\n" +
			"        \n" +
			"            continue;\n" +
			"        }\n" +
			"        \n" +
			"        if(minNanos>values[i].nanos){\n" +
			"            minNanos = values[i].nanos;\n" +
			"        } else if(maxNanos<values[i].nanos){\n" +
			"            maxNanos = values[i].nanos;\n" +
			"        }            \n" +
			"    }\n" +
			"    \n" +
			"    return {\"count\":count, \"threads\":threads, \"times\":times, \"minNanos\":minNanos, \"maxNanos\":maxNanos, \"totalTime\":sumTime};\n" +
			"}";

		BasicDBObject query = new BasicDBObject("appId", appId).append("sessionId", sessionId);

		if(recId>-1) {
			query.append("recId", recId);
		}

		String tempCollection = "methods."+ UUID.randomUUID().toString();

		MapReduceCommand cmd = new MapReduceCommand( statistics , map , reduce , tempCollection , MapReduceCommand.OutputType.REPLACE, query );
		statistics.mapReduce(cmd);

		Collection<MethodStatSample> result = new ArrayList<>();

		DBCollection mapreduce = db.getCollection(tempCollection);
		DBCursor cursor = mapreduce.find().sort(new BasicDBObject("value.count", -1).append("value.totalTime", -1));

		while(cursor.hasNext()){
			DBObject item0 = cursor.next();
			BasicDBObject item = (BasicDBObject) item0.get("value");
			MethodStatSample sample = new MethodStatSample((String)item0.get("_id"), ((Double)item.get("count")).longValue(), ((Double)item.get("minNanos")).longValue(), ((Double)item.get("maxNanos")).longValue(), ((Double)item.get("totalTime")).longValue());

			for(Object element:(BasicDBList) item.get("threads")){
				sample.threads.add(((Double)element).longValue());
			}
			result.add(sample);
		}

		mapreduce.drop();
		return result;
	}
}
