package com.focusit.agent.analyzer.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by Denis V. Kirpichenkov on 14.12.14.
 */
@Controller
public class IndexController {

		@RequestMapping("/")
		public String welcome(ModelAndView modelAndView) {
			return "index";
		}
}
