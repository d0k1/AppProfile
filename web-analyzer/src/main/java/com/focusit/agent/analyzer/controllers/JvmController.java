package com.focusit.agent.analyzer.controllers;

import com.focusit.agent.analyzer.dao.jvm.JvmDao;
import com.focusit.agent.analyzer.data.jvm.HeapSample;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Collection;

/**
 * Created by Denis V. Kirpichenkov on 24.12.14.
 */
@RestController
@RequestMapping("/jvm")
public class JvmController {

	@Inject
	JvmDao dao;

	@RequestMapping(value = "/{sessionId}/heap", method = RequestMethod.GET)
	public Collection<HeapSample> heapAll(@PathVariable("sessionId") String sessionId){
		return dao.getAllHeapData(Long.parseLong(sessionId));
	}

	@RequestMapping(value = "/{sessionId}/heap/last/{seconds}", method = RequestMethod.GET)
	public Collection<HeapSample> heapAll(@PathVariable("sessionId") String sessionId, @PathVariable("seconds") String seconds){
		return dao.getLastHeapData(Long.parseLong(sessionId), Long.parseLong(seconds));
	}
}
