package com.focusit.agent.bond;

import com.focusit.agent.bond.time.GlobalTime;
import com.focusit.utils.metrics.store.StorageManager;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;

/**
 * Agent main class. Loading desired class transformer
 * <p/>
 * Created by Denis V. Kirpichenkov on 06.08.14.
 */
public class Agent {
	private static Instrumentation agentInstrumentation = null;
	private static final Logger LOG = LoggerFactory.getLogger(Agent.class);

	public static void premain(String agentArguments, Instrumentation instrumentation) throws IOException, UnmodifiableClassException {
		try {
			agentmain(agentArguments, instrumentation);
		} catch (Throwable e) {
			LOG.error("Agent loading error", e);
			throw e;
		}
	}

	private static void modifyBootstrapClasspath(Instrumentation instrumentation) throws IOException {
		if (AgentManager.agentJar != null) {
			instrumentation.appendToBootstrapClassLoaderSearch(AgentManager.agentJar);
		}

		for (URL url : ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs()) {
			if (url.getFile().contains("slf4j-api")) {
				instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(url.getFile()));
			} else if (url.getFile().contains("slf4j-log4j12")) {
				instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(url.getFile()));
			} else if (url.getFile().contains("log4j-")) {
				instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(url.getFile()));
			} else if (url.getFile().contains("commons-lang3-")) {
				instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(url.getFile()));
			} else if (url.getFile().contains("javassist")) {
				instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(url.getPath()));
			}
		}
	}

	private static void setupLogging() {
		System.setProperty("log4j.ignoreTCL", "true");
		PropertyConfigurator.configure(AgentConfiguration.getAgentLog4jProps());
	}

	private static boolean isClassExcluded(String className) {
		String excludes[] = AgentConfiguration.getExcludeClasses();
		String ignoreExcludes[] = AgentConfiguration.getIgnoreExcludeClasses();

		if (excludes != null) {
			boolean skip = false;

			for (int i = 0; i < excludes.length; i++) {
				if (className.startsWith(excludes[i])) {
					skip = true;
					break;
				}
			}

			for (int i = 0; i < ignoreExcludes.length; i++) {
				if (className.startsWith(ignoreExcludes[i])) {
					skip = false;
					break;
				}
			}

			if (skip) {
				return true;
			}
		}

		return false;
	}

	private static void retransformAlreadyLoadedClasses(Instrumentation instrumentation) {
		for (Class cls : instrumentation.getAllLoadedClasses()) {

			if (isClassExcluded(cls.getName()))
				continue;

			if (instrumentation.isModifiableClass(cls)) {
				try {
					instrumentation.retransformClasses(cls);
				} catch (UnmodifiableClassException e) {
					LOG.error("unmodifiable class {}", cls.getName());
				}
			}
		}
	}

	public static void agentmain(String agentArguments, Instrumentation instrumentation) throws IOException, UnmodifiableClassException {
		try {
			agentInstrumentation = instrumentation;

			modifyBootstrapClasspath(instrumentation);
			setupLogging();

			if (!AgentConfiguration.isAgentEnabled()) {
				LOG.info("Agent is disabled");
				return;
			}

			LOG.info("Loading bond agent");

			String excludes[] = AgentConfiguration.getExcludeClasses();
			String ignoreExcludes[] = AgentConfiguration.getIgnoreExcludeClasses();

			AgentConfiguration.Transformer transformer = AgentConfiguration.getAgentClassTransformer();

			// strange bu usage SWITCH causes IllegalAccessException but IF is OK
			if (transformer == AgentConfiguration.Transformer.asm) {
				agentInstrumentation.addTransformer(new AsmClassTransformer(excludes, ignoreExcludes, instrumentation), true);
			} else if (transformer == AgentConfiguration.Transformer.javaassist) {
				agentInstrumentation.addTransformer(new JavaAssistClassTransformer(excludes, ignoreExcludes, instrumentation), true);
			} else if (transformer == AgentConfiguration.Transformer.cglib) {
				agentInstrumentation.addTransformer(new CGLibClassTransformer(excludes, ignoreExcludes, instrumentation), true);
			}

			retransformAlreadyLoadedClasses(instrumentation);

			startStorage();
		} catch (Throwable e) {
			LOG.error("Error loading agent", e);
		}
	}

	private static void startStorage() throws FileNotFoundException {
		GlobalTime gt = new GlobalTime(AgentConfiguration.getTimerPrecision());
		gt.start();

		final StorageManager storage = new StorageManager();
		storage.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					storage.exit();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				try {
					storage.dumpRest();
				} catch (Throwable e) {
					LOG.error("Shutdown hook error: " + e.getMessage(), e);
				}
			}
		});
	}


	private static void printClasspath() {
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		URL[] urls = ((URLClassLoader) cl).getURLs();
		for (URL url : urls) {
			System.out.println(url.getFile());
		}
	}
}
