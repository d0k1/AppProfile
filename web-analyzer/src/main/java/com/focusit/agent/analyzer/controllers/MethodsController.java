package com.focusit.agent.analyzer.controllers;

import com.focusit.agent.analyzer.dao.report.MethodReportDao;
import com.focusit.agent.analyzer.data.statistics.MethodReportSample;
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
	MethodReportDao reportDao;

	@RequestMapping(value = "/{appId}/{sessionId}/{recId}/analyze", method = RequestMethod.GET)
	public boolean analyze(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId, @PathVariable("recId") String recId){
		return reportDao.analyzeReport(Long.parseLong(appId), Long.parseLong(sessionId), Long.parseLong(recId));
	}

	@RequestMapping(value = "/{appId}/{sessionId}/{recId}/report", method = RequestMethod.GET)
	public Collection<MethodReportSample> methods(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId, @PathVariable("recId") String recId){
		return reportDao.getMethodsReport(Long.parseLong(appId), Long.parseLong(sessionId), Long.parseLong(recId));
	}
}
