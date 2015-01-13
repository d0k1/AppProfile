package com.focusit.agent.analyzer.controllers;

import com.focusit.agent.analyzer.dao.jvm.JvmDao;
import com.focusit.agent.analyzer.data.jvm.CpuSample;
import com.focusit.agent.analyzer.data.jvm.HeapSample;
import com.focusit.agent.analyzer.data.jvm.JvmSamplesCounter;
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

	@RequestMapping(value = "/{appId}/{sessionId}/{recId}/heap/last/{seconds}", method = RequestMethod.GET)
	public Collection<HeapSample> heapLast(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId, @PathVariable("recId") String recId, @PathVariable("seconds") String seconds){
		return dao.getLastHeapData(Long.parseLong(appId), Long.parseLong(sessionId), Long.parseLong(recId), Long.parseLong(seconds));
	}

	@RequestMapping(value = "/{appId}/{sessionId}/{recId}/heap/{max}/{min}", method = RequestMethod.GET)
	public Collection<HeapSample> heap(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId, @PathVariable("recId") String recId, @PathVariable("max") String max, @PathVariable("min") String min){
		return dao.getHeapData(Long.parseLong(appId), Long.parseLong(sessionId), Long.parseLong(recId), Long.parseLong(min), Long.parseLong(max));
	}

	@RequestMapping(value = "/{appId}/{sessionId}/{recId}/cpu/last/{seconds}", method = RequestMethod.GET)
	public Collection<CpuSample> cpuLast(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId, @PathVariable("recId") String recId, @PathVariable("seconds") String seconds){
		return dao.getLastCpuData(Long.parseLong(appId), Long.parseLong(sessionId), Long.parseLong(recId), Long.parseLong(seconds));
	}

	@RequestMapping(value = "/{appId}/{sessionId}/{recId}/cpu/{max}/{min}", method = RequestMethod.GET)
	public Collection<CpuSample> cpu(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId, @PathVariable("recId") String recId, @PathVariable("max") String max, @PathVariable("min") String min){
		return dao.getCpuData(Long.parseLong(appId), Long.parseLong(sessionId), Long.parseLong(recId), Long.parseLong(min), Long.parseLong(max));
	}

	@RequestMapping(value = "/{appId}/{sessionId}/{recId}/count", method = RequestMethod.GET)
	public JvmSamplesCounter count(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId, @PathVariable("recId") String recId){
		return dao.getJvmSamplesCount(Long.parseLong(appId), Long.parseLong(sessionId), Long.parseLong(recId));
	}
}
