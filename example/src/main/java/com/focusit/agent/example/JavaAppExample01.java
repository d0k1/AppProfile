package com.focusit.agent.example;

import com.focusit.agent.loader.jassie.AgentLoader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Example of using agent
 * Created by Denis V. Kirpichenkov on 07.08.14.
 */
public class JavaAppExample01 {

	public static void main(String[] args) throws SQLException, IOException, URISyntaxException {

		AgentLoader.loadAgent();

		String url = "jdbc:postgresql://localhost/example";
		Properties props = new Properties();
		props.setProperty("user","stand");
		props.setProperty("password","stand");
		Connection conn = DriverManager.getConnection(url, props);

		conn.close();
	}
}
