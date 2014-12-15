package com.focusit.agent.example.example01;

/**
 * Example of using agent
 * Created by Denis V. Kirpichenkov on 07.08.14.
 */
public class JavaAppExample02 {

	public static void main(String[] args) {
//		try {
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
			System.err.println("Begin!");
			new ClassToInstrument().foo();
			System.err.println("Done!");
//		} catch (Throwable e) {
//			System.err.println("Error: " + e.getMessage());
//			throw e;
//		}
	}
}
