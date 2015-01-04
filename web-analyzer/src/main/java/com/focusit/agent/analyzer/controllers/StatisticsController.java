package com.focusit.agent.analyzer.controllers;

import com.focusit.agent.analyzer.dao.statistics.StatisticsDao;
import com.focusit.agent.analyzer.data.statistics.MethodCallSample;
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

	@RequestMapping(value = "/{appId}/{sessionId}/{recId}/methods", method = RequestMethod.GET)
	public Collection<MethodCallSample> methods(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId, @PathVariable("recId") String recId){
		return dao.getMethods(Long.parseLong(appId), Long.parseLong(sessionId), Long.parseLong(recId));
	}

	@RequestMapping(value = "/{appId}/{sessionId}/{recId}/analyze", method = RequestMethod.GET)
	public boolean analyze(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId, @PathVariable("recId") String recId){
		return dao.analyzeSession(Long.parseLong(appId), Long.parseLong(sessionId), Long.parseLong(recId));
	}

}
