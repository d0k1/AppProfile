package com.focusit.agent.analyzer.configuration;

import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
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

	public static final String SESSIONS_COLLECTION = "sessions";

	@Override
	protected String getDatabaseName() {
		return DB;
	}

	@Bean
	@Override
	public Mongo mongo() throws Exception {
		return new MongoClient(MONGO_DB_HOST, MONGO_DB_PORT);
	}

	@Bean
	@Override
	public MongoTemplate mongoTemplate() throws Exception {
		return super.mongoTemplate();
	}

	@Bean(name=SESSIONS_COLLECTION)
	public DBCollection getDbCollection() throws Exception {
		return mongoDbFactory().getDb().getCollection(SESSIONS_COLLECTION);
	}
}
