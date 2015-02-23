package com.focusit.agent.analyzer.configuration;

import com.mongodb.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Created by Denis V. Kirpichenkov on 25.12.14.
 */
@Configuration
@PropertySource("classpath:mongo.properties")
public class MongoConfiguration extends AbstractMongoConfiguration {

	@Value("${host}")
	private String MONGO_DB_HOST;

	@Value("${port}")
	private int MONGO_DB_PORT;

	@Value("${db}")
	private String DB;

	public static final String RECORDS_COLLECTION = "records";
	public static final String SESSIONS_COLLECTION = "sessions";
	public static final String JVM_COLLECTION = "jvmmonitoring";
	public static final String OS_COLLECTION = "osmonitoring";
	public static final String METHODSMAP_COLLECTION = "methodsmap";
	public static final String STATISTICS_COLLECTION = "statistics";
	public static final String REPORT_COLLECTION = "methodsreport";

	@Override
	protected String getDatabaseName() {
		return DB;
	}

	@Bean
	@Override
	public Mongo mongo() throws Exception {
		return new MongoClient(new ServerAddress(MONGO_DB_HOST, MONGO_DB_PORT), MongoClientOptions.builder().writeConcern(WriteConcern.NORMAL).build());
	}

	@Bean
	@Override
	public MongoTemplate mongoTemplate() throws Exception {
		return super.mongoTemplate();
	}

	@Bean(name=SESSIONS_COLLECTION)
	public DBCollection getDbCollectionSessions() throws Exception {
		return mongoDbFactory().getDb().getCollection(SESSIONS_COLLECTION);
	}

	@Bean(name=JVM_COLLECTION)
	public DBCollection getDbCollectionJvm() throws Exception {
		return mongoDbFactory().getDb().getCollection(JVM_COLLECTION);
	}

	@Bean(name=METHODSMAP_COLLECTION)
	public DBCollection getDbCollectionMethods() throws Exception {
		return mongoDbFactory().getDb().getCollection(METHODSMAP_COLLECTION);
	}

	@Bean(name=STATISTICS_COLLECTION)
	public DBCollection getDbCollectionStatistics() throws Exception {
		return mongoDbFactory().getDb().getCollection(STATISTICS_COLLECTION);
	}

	@Bean(name=RECORDS_COLLECTION)
	public DBCollection getDbCollectionRecords() throws Exception {
		return mongoDbFactory().getDb().getCollection(RECORDS_COLLECTION);
	}

	@Bean(name=REPORT_COLLECTION)
	public DBCollection getDbCollectionReport() throws Exception {
		return mongoDbFactory().getDb().getCollection(REPORT_COLLECTION);
	}

	@Bean(name=OS_COLLECTION)
	public DBCollection getDbCollectionOS() throws Exception {
		return mongoDbFactory().getDb().getCollection(OS_COLLECTION);
	}
	@Bean
	public com.mongodb.DB getDb() throws Exception {
		return mongoDbFactory().getDb();
	}
}
