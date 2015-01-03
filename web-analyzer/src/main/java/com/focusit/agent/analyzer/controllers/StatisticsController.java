package com.focusit.agent.analyzer.controllers;

import com.focusit.agent.analyzer.dao.statistics.StatisticsDao;
import com.focusit.agent.analyzer.data.statistics.MethodCallSample;
import com.focusit.agent.analyzer.data.statistics.ThreadSample;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Collection;

/**
 * Created by Denis V. Kirpichenkov on 03.01.15.
 */
@RestController
@RequestMapping("/profiler")
public class StatisticsController {
	@Inject
	StatisticsDao dao;

	@RequestMapping(value = "/{appId}/{sessionId}/methods", method = RequestMethod.GET)
	public Collection<MethodCallSample> methods(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId){
		return dao.getMethods(Long.parseLong(appId), Long.parseLong(sessionId));
	}

	@RequestMapping(value = "/{appId}/{sessionId}/threads", method = RequestMethod.GET)
	public Collection<ThreadSample> threads(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId){
		return dao.getThreads(Long.parseLong(appId), Long.parseLong(sessionId));
	}

	@RequestMapping(value = "/{appId}/{sessionId}/analyze", method = RequestMethod.GET)
	public boolean analyze(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId){
		return dao.analyzeSession(Long.parseLong(appId), Long.parseLong(sessionId));
	}

}
