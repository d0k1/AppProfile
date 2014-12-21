package com.focusit.agent.example.example01;

import com.focusit.agent.loader.jassie.AgentLoader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

/**
 * Example of using agent
 * Created by Denis V. Kirpichenkov on 07.08.14.
 */
public class JavaAppExample01 {
	static {
		try {
			AgentLoader.loadAgent(JavaAppExample01.class.getClassLoader());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws SQLException, IOException, URISyntaxException {
		try {
//			String url = "jdbc:postgresql://localhost/example";
//			Properties props = new Properties();
//			props.setProperty("user", "stand");
//			props.setProperty("password", "stand");
//			Connection conn = DriverManager.getConnection(url, props);
//
//			try (PreparedStatement pstmt = conn.prepareStatement("SELECT ?")) {
//				pstmt.setInt(1, 1);
//				try (ResultSet rs = pstmt.executeQuery()) {
//					System.out.println("Executed");
//				}
//			}
//
//			new ClassToInstrument().foo();
			new ClassToInstrument().bar2();
//			new ClassToInstrument().bar2();
//			new ClassToInstrument().bar2();
//			new ClassToInstrument().bar2();
		} catch (Throwable e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}
