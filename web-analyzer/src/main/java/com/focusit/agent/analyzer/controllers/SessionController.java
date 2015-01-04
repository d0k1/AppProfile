package com.focusit.agent.analyzer.controllers;

import com.focusit.agent.analyzer.dao.sessions.SessionDao;
import com.focusit.agent.analyzer.data.sessions.AppInfo;
import com.focusit.agent.analyzer.data.sessions.RecordInfo;
import com.focusit.agent.analyzer.data.sessions.SessionInfo;
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
@RequestMapping("/sessions")
public class SessionController {

	@Inject
	SessionDao dao;

	@RequestMapping(value = "/{appId}/{sessionId}", method = RequestMethod.GET)
	public Collection<RecordInfo> record(@PathVariable("appId") String appId, @PathVariable("sessionId") String sessionId){
		return dao.getRecords(Long.parseLong(appId), Long.parseLong(sessionId));
	}

	@RequestMapping(value = "/{appId}", method = RequestMethod.GET)
	public Collection<SessionInfo> sessions(@PathVariable("appId") String appId){
		return dao.getSessions(Long.parseLong(appId));
	}

	@RequestMapping(value = "/apps", method = RequestMethod.GET)
	public Collection<AppInfo> appIds(){
		return dao.getAppIds();
	}
}
