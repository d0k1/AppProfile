package com.focusit.agent.mongo;

import com.mongodb.DB;

/**
 * Generic interface to data loaders
 * <p/>
 * Created by Denis V. Kirpichenkov on 14.12.14.
 */
public interface MongoLoader {
	void loadData(DB bondDb, long sessionId);
}
