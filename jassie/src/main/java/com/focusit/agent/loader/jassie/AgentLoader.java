package com.focusit.agent.loader.jassie;

import com.focusit.agent.bond.AgentManager;
import com.sun.tools.attach.VirtualMachine;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.util.jar.JarFile;

/**
 * Java agent runtime loader
 *
 * Created by Denis V. Kirpichenkov on 06.08.14.
 */
public class AgentLoader {

	public static void loadAgent() throws URISyntaxException, IOException {

		InputStream in = AgentLoader.class.getResourceAsStream("/bond.jar");

		File temp = File.createTempFile("bond", ".jar");
		String jarPath = temp.getAbsolutePath();
		temp.deleteOnExit();

		try (FileOutputStream out = new FileOutputStream(temp)) {
			IOUtils.copy(in, out);
		}

		AgentManager.agentJar = new JarFile(jarPath);

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
}
