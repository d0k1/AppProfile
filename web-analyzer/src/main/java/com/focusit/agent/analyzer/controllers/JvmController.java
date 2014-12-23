package com.focusit.agent.analyzer.controllers;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by Denis V. Kirpichenkov on 24.12.14.
 */
@RestController
@RequestMapping("/jvm")
public class JvmController {

	@RequestMapping(value = "/{sessionId}/samples", method = RequestMethod.GET)
	public long samplesTotal(@PathVariable("sessionId") String sessionId){
		return 100+Integer.parseInt(sessionId);
	}
}
