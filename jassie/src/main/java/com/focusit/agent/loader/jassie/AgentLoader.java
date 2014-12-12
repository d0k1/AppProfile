package com.focusit.agent.loader.jassie;

import com.focusit.agent.bond.AgentManager;
import com.sun.tools.attach.VirtualMachine;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

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

		AgentManager.appClassloader = (URLClassLoader) loader;

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

		String args = generateArgs(jarPath);
		try {
			VirtualMachine vm = VirtualMachine.attach(pid);
			vm.loadAgent(jarPath, args);
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

	private static String generateArgs(String agentJar) {
		List<String> classpath = getJars(AgentManager.appClassloader);
		classpath.add(agentJar);

		List<String> neededJars = new ArrayList<>();

		for (String jar : classpath) {
			if (jar.toLowerCase().contains("slf4j-api")) {
				neededJars.add(jar);
			} else if (jar.toLowerCase().contains("slf4j-log4j12")) {
				neededJars.add(jar);
			} else if (jar.toLowerCase().contains("log4j-")) {
				neededJars.add(jar);
			} else if (jar.toLowerCase().contains("commons-lang3-")) {
				neededJars.add(jar);
			} else if (jar.toLowerCase().contains("javassist")) {
				neededJars.add(jar);
			}
		}
		neededJars.add(agentJar);

		System.setProperty("agent.search.classpath", StringUtils.join(classpath, ','));
//		modifySystemClassloader(jars);
		String result = StringUtils.join(neededJars, ',');
		return result;
	}

	private static void modifySystemClassloader(String... jars) {
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class sysclass = URLClassLoader.class;

		Class[] parameters = new Class[]{URL.class};

		List<String> loaded = new ArrayList<>();

		for (URL url : sysloader.getURLs()) {
			loaded.add(url.getFile().toLowerCase());
		}

		try {
			Method method = sysclass.getDeclaredMethod("addURL", parameters);
			method.setAccessible(true);
			for (String jar : jars) {
				if (loaded.contains(jar.toLowerCase()))
					continue;

				method.invoke(sysloader, new File(jar).toURL());
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}//end try catch

	}
}
