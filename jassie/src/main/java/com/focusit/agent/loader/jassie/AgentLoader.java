package com.focusit.agent.loader.jassie;

import com.sun.tools.attach.VirtualMachine;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Java agent runtime loader
 *
 * Created by Denis V. Kirpichenkov on 06.08.14.
 */
public class AgentLoader {

	public static void loadAgent() throws IOException, URISyntaxException {
		loadAgent(null);
	}

	public static void loadAgent(ClassLoader loader) throws URISyntaxException, IOException {
		InputStream in = AgentLoader.class.getResourceAsStream("/bond.jar");

		File temp = File.createTempFile("bond", ".jar");
		String jarPath = temp.getAbsolutePath();
		temp.deleteOnExit();

		try (FileOutputStream out = new FileOutputStream(temp)) {
			IOUtils.copy(in, out);
		}

		generateArgs((URLClassLoader) loader, jarPath);

		String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
		int p = nameOfRunningVM.indexOf('@');
		String pid = nameOfRunningVM.substring(0, p);

		try {
			VirtualMachine vm = VirtualMachine.attach(pid);
			vm.loadAgent(jarPath);//, args);
			vm.detach();
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	private static List<String> getJars(URLClassLoader loader) {
		if (loader == null) {
			return new ArrayList<>();
		}

		List<String> jars = new ArrayList<>();
		for (URL url : loader.getURLs()) {
			if (url.getFile().toLowerCase().endsWith(".jar"))
				jars.add(url.getFile());
		}

		if (loader.getParent() instanceof URLClassLoader) {
			jars.addAll(getJars((URLClassLoader) loader.getParent()));
		}

		return jars;
	}

	private static void generateArgs(URLClassLoader loader, String agentJar) {
		List<String> classpath = getJars(loader);

		System.setProperty("agent.jar", agentJar);
		System.setProperty("agent.search.classpath", StringUtils.join(classpath, ','));
	}
}
