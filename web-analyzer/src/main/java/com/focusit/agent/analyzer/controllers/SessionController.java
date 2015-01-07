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

	@RequestMapping(value = "/{appId}/startprofiling", method = RequestMethod.GET)
	public Map<String, String> startprofiling(@PathVariable("appId") String appId){
		sessionManager.setProfilingEnabled(Long.parseLong(appId), true);
		return getSettings(Long.parseLong(appId));
	}

	@RequestMapping(value = "/{appId}/startmonitoring", method = RequestMethod.GET)
	public Map<String, String> startmonitoring(@PathVariable("appId") String appId){
		sessionManager.setMonitoringEnabled(Long.parseLong(appId), true);
		return getSettings(Long.parseLong(appId));
	}

	@RequestMapping(value = "/{appId}/stopprofiling", method = RequestMethod.GET)
	public Map<String, String> stopprofiling(@PathVariable("appId") String appId){
		sessionManager.setProfilingEnabled(Long.parseLong(appId), false);
		return getSettings(Long.parseLong(appId));
	}

	@RequestMapping(value = "/{appId}/stopmonitoring", method = RequestMethod.GET)
	public Map<String, String> stopmonitoring(@PathVariable("appId") String appId){
		sessionManager.setMonitoringEnabled(Long.parseLong(appId), false);
		return getSettings(Long.parseLong(appId));
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

	private Map<String, String> getAutoSettings(){
		Map<String, String> result = new HashMap<>();
		result.put("automonitoring", String.valueOf(sessionManager.isAutomonitoring()));
		result.put("autoprofiling", String.valueOf(sessionManager.isAutoprofiling()));

		return result;
	}

	private Map<String, String> getSettings(long appId){
		Map<String, String> result = new HashMap<>();
		result.put("monitoring", String.valueOf(sessionManager.isMonitoringEnabled(appId)));
		result.put("profiling", String.valueOf(sessionManager.isProfilingEnabled(appId)));

		return result;
	}

	@RequestMapping(value = "/settings", method = RequestMethod.GET)
	public Map<String, String> getSystemAutoSettings(){
		return getAutoSettings();
	}

	@RequestMapping(value = "/settings/{appId}", method = RequestMethod.GET)
	public Map<String, String> getAppSettings(@PathVariable("appId") String appId){
		return getSettings(Long.parseLong(appId));
	}

	@RequestMapping(value = "/autoprofiling/enable", method = RequestMethod.GET)
	public Map<String, String> autoprofilingEnable(){
		sessionManager.setAutoprofiling(true);
		return getAutoSettings();
	}

	@RequestMapping(value = "/autoprofiling/disable", method = RequestMethod.GET)
	public Map<String, String> autoprofilingDisable(){
		sessionManager.setAutoprofiling(false);
		return getAutoSettings();
	}

	@RequestMapping(value = "/automonitoring/enable", method = RequestMethod.GET)
	public Map<String, String> automonitoringEnable(){
		sessionManager.setAutomonitoring(true);
		return getAutoSettings();
	}

	@RequestMapping(value = "/automonitoring/disable", method = RequestMethod.GET)
	public Map<String, String> automonitoringDisable(){
		sessionManager.setAutomonitoring(false);
		return getAutoSettings();
	}

}
