package com.focusit.agent.analyzer.controllers;

import com.focusit.agent.analyzer.dao.statistics.StatisticsDao;
import com.focusit.agent.analyzer.data.statistics.MethodStatSample;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Collection;

/**
 * Created by Denis V. Kirpichenkov on 18.01.15.
 */
@RestController
@RequestMapping("/methods")
public class MethodsController {
	@Inject
	StatisticsDao dao;

	@RequestMapping(value = "/{appId}/{sessionId}/{recId}", method = RequestMethod.GET)
	public Collection<MethodStatSample> analyze(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId, @PathVariable("recId") String recId){
		return dao.getMethodsStat(Long.parseLong(appId), Long.parseLong(sessionId), Long.parseLong(recId));
	}
}
