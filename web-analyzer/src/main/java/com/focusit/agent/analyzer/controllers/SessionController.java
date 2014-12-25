package com.focusit.agent.analyzer.controllers;

import com.focusit.agent.analyzer.dao.sessions.SessionDao;
import com.focusit.agent.analyzer.data.sessions.SessionInfo;
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

	@RequestMapping(method = RequestMethod.GET)
	public Collection<SessionInfo> sessions(){
		return dao.getSessions();
	}
}
