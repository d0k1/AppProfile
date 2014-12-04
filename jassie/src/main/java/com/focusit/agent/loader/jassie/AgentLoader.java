package com.focusit.agent.loader.jassie;

import com.sun.tools.attach.VirtualMachine;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URLDecoder;

/**
 * Java agent runtime loader
 *
 * Created by Denis V. Kirpichenkov on 06.08.14.
 */
public class AgentLoader {
	public static void addShutdownHook() {
		try {
			com.focusit.agent.bond.AgentManager manager = (com.focusit.agent.bond.AgentManager) Class.forName("com.focusit.agent.bond.AgentManager").newInstance();
			manager.addShutodwnHook();
		} catch (ClassNotFoundException e){
			System.err.println(e.getMessage());
		} catch (InstantiationException e1){
			System.err.println(e1.getMessage());
		} catch (IllegalAccessException e2){
			System.err.println(e2.getMessage());
		}
	}
	public static void loadAgent() throws URISyntaxException, IOException {

		InputStream in = AgentLoader.class.getResourceAsStream("/bond.jar");

		File temp = File.createTempFile("bond", ".jar");
		temp.deleteOnExit();

		try (FileOutputStream out = new FileOutputStream(temp)) {
			IOUtils.copy(in, out);
		}

		String jarPath = temp.getAbsolutePath();

		String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
		int p = nameOfRunningVM.indexOf('@');
		String pid = nameOfRunningVM.substring(0, p);

		try {
			VirtualMachine vm = VirtualMachine.attach(pid);
			vm.loadAgent(jarPath);
			vm.detach();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Just test of possibility of loading agent at runtime
	 * @param args
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public static void main(String[] args) throws IOException, URISyntaxException {
		new AgentLoader().loadAgent();
	}
}
