package com.focusit.agent.analyzer.controllers;

import com.focusit.agent.analyzer.dao.statistics.StatisticsDao;
import com.focusit.agent.analyzer.data.statistics.MethodCallSample;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.Collection;

/**
 * Created by Denis V. Kirpichenkov on 03.01.15.
 */
@RestController
@RequestMapping("/profiler")
public class ProfilerController {
	@Inject
	StatisticsDao dao;

	@RequestMapping(value = "/{appId}/{sessionId}/{recId}/methods", method = RequestMethod.GET)
	public Collection<MethodCallSample> methods(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId, @PathVariable("recId") String recId){
		return dao.getMethods(Long.parseLong(appId), Long.parseLong(sessionId), Long.parseLong(recId));
	}

	@RequestMapping(value = "/{appId}/{sessionId}/{recId}/methods", method = RequestMethod.POST)
	public Collection<MethodCallSample> methodsByParent(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId, @PathVariable("recId") String recId, @RequestBody String[] parents){
		return dao.getMethodsByParents(Long.parseLong(appId), Long.parseLong(sessionId), Long.parseLong(recId), parents);
	}

	@RequestMapping(value = "/{appId}/{sessionId}/{recId}/analyze", method = RequestMethod.GET)
	public boolean analyze(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId, @PathVariable("recId") String recId){
		return dao.analyzeSession(Long.parseLong(appId), Long.parseLong(sessionId), Long.parseLong(recId));
	}
}
