package com.focusit.agent.analyzer.controllers;

import com.focusit.agent.analyzer.dao.sessions.SessionDao;
import com.focusit.agent.analyzer.data.netty.session.NettySessionManager;
import com.focusit.agent.analyzer.data.sessions.AppInfo;
import com.focusit.agent.analyzer.data.sessions.RecordInfo;
import com.focusit.agent.analyzer.data.sessions.SessionInfo;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Denis V. Kirpichenkov on 24.12.14.
 */
@RestController
@RequestMapping("/sessions")
public class SessionController {

	@Inject
	SessionDao dao;

	@Inject
	NettySessionManager sessionManager;

	@RequestMapping(value = "/{appId}/{sessionId}", method = RequestMethod.GET)
	public Collection<RecordInfo> record(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId){
		return dao.getRecords(Long.parseLong(appId), Long.parseLong(sessionId));
	}

	@RequestMapping(value = "/{appId}/{sessionId}/startrofiling", method = RequestMethod.GET)
	public boolean startprofiling(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId){
		sessionManager.setProfilingEnabled(Long.parseLong(appId), true);
		return true;
	}

	@RequestMapping(value = "/{appId}/{sessionId}/startmonitoring", method = RequestMethod.GET)
	public boolean startmonitoring(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId){
		sessionManager.setMonitoringEnabled(Long.parseLong(appId), true);
		return true;
	}

	@RequestMapping(value = "/{appId}/{sessionId}/stopprofiling", method = RequestMethod.GET)
	public boolean stopprofiling(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId){
		sessionManager.setProfilingEnabled(Long.parseLong(appId), false);
		return true;
	}

	@RequestMapping(value = "/{appId}/{sessionId}/stopmonitoring", method = RequestMethod.GET)
	public boolean stopmonitoring(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId){
		sessionManager.setMonitoringEnabled(Long.parseLong(appId), false);
		return true;
	}

	@RequestMapping(value = "/{appId}/{sessionId}/stoprecord", method = RequestMethod.GET)
	public boolean stoprecord(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId){
		sessionManager.setProfilingEnabled(Long.parseLong(appId), false);
		sessionManager.setMonitoringEnabled(Long.parseLong(appId), false);
		return true;
	}

	@RequestMapping(value = "/{appId}/{sessionId}/newrecord", method = RequestMethod.GET)
	public boolean newrecord(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId){
		sessionManager.startRecording(Long.parseLong(appId), Long.parseLong(sessionId));
		return true;
	}

	@RequestMapping(value = "/{appId}", method = RequestMethod.GET)
	public Collection<SessionInfo> sessions(@PathVariable("appId") String appId){
		return dao.getSessions(Long.parseLong(appId));
	}

	@RequestMapping(value = "/apps", method = RequestMethod.GET)
	public Collection<AppInfo> appIds(){
		return dao.getAppIds();
	}

	@RequestMapping(value = "/settings", method = RequestMethod.GET)
	public Map<String, String> sessionManagerSettings(){
		Map<String, String> result = new HashMap<>();
		result.put("automonitoring", String.valueOf(sessionManager.isAutomonitoring()));
		result.put("autoprofiling", String.valueOf(sessionManager.isAutoprofiling()));

		return result;
	}

	@RequestMapping(value = "/autoprofiling/enable", method = RequestMethod.GET)
	public boolean autoprofilingEnable(@PathVariable("appId") String appId){
		sessionManager.setAutoprofiling(true);
		return true;
	}

	@RequestMapping(value = "/autoprofiling/disable", method = RequestMethod.GET)
	public boolean autoprofilingDisable(@PathVariable("appId") String appId){
		sessionManager.setAutoprofiling(false);
		return true;
	}

	@RequestMapping(value = "/automonitoring/enable", method = RequestMethod.GET)
	public boolean automonitoringEnable(){
		sessionManager.setAutomonitoring(true);
		return true;
	}

	@RequestMapping(value = "/automonitoring/disable", method = RequestMethod.GET)
	public boolean automonitoringDisable(@PathVariable("appId") String appId){
		sessionManager.setAutomonitoring(false);
		return true;
	}

}
