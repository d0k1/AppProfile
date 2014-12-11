package com.focusit.agent.example;

import com.focusit.agent.loader.jassie.AgentLoader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

/**
 * Created by Denis V. Kirpichenkov on 12.12.14.
 */
@WebServlet("/test")
public class TestServlet extends HttpServlet {
	static {
		try {
			System.err.println("Current dir: " + Paths.get("").toAbsolutePath().toString());
			AgentLoader.loadAgent(TestServlet.class.getClassLoader());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getWriter().print("Test servlet");
	}
}
