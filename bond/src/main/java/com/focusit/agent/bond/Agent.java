package com.focusit.agent.bond;

import com.focusit.agent.bond.time.GlobalTime;
import com.focusit.utils.metrics.MethodsMapDumper;
import com.focusit.utils.metrics.StatisticDumper;

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

	public static void premain(String agentArguments, Instrumentation instrumentation) throws IOException, UnmodifiableClassException {
		agentmain(agentArguments, instrumentation);
	}

	private static void modifyBootstrapClasspath(Instrumentation instrumentation) throws IOException {
		if (AgentManager.agentJar != null) {
			instrumentation.appendToBootstrapClassLoaderSearch(AgentManager.agentJar);
		}

		URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		for (URL url : systemClassLoader.getURLs()) {
			if (url.getProtocol() == "file" && url.getFile().contains("javassist")) {
				instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(url.getPath()));
			}
		}
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

	public static void retransformAlreadyLoadedClasses(Instrumentation instrumentation) {
		for (Class cls : instrumentation.getAllLoadedClasses()) {

			if (isClassExcluded(cls.getName()))
				continue;

			if (instrumentation.isModifiableClass(cls)) {
				try {
					instrumentation.retransformClasses(cls);
				} catch (UnmodifiableClassException e) {
					System.err.println("unmodifiable class " + cls.getName());
				}
			}
		}
	}

	public static void agentmain(String agentArguments, Instrumentation instrumentation) throws IOException, UnmodifiableClassException {

		if (!AgentConfiguration.isAgentEnabled()) {
			return;
		}

		modifyBootstrapClasspath(instrumentation);

		String excludes[] = AgentConfiguration.getExcludeClasses();
		String ignoreExcludes[] = AgentConfiguration.getIgnoreExcludeClasses();

		AgentConfiguration.Transformer transformer = AgentConfiguration.getAgentClassTransformer();

		agentInstrumentation = instrumentation;

		// strange bu usage SWITCH causes IllegalAccessException but IF is OK
		if (transformer == AgentConfiguration.Transformer.asm) {
			agentInstrumentation.addTransformer(new AsmClassTransformer(excludes, ignoreExcludes, instrumentation), true);
		} else if (transformer == AgentConfiguration.Transformer.javaassist) {
			agentInstrumentation.addTransformer(new JavaAssistClassTransformer(excludes, ignoreExcludes, instrumentation), true);
		} else if (transformer == AgentConfiguration.Transformer.cglib) {

		}

		retransformAlreadyLoadedClasses(instrumentation);

		startDumpers();
	}

	private static void startDumpers() throws FileNotFoundException {
		GlobalTime gt = new GlobalTime(10);
		gt.start();

		final StatisticDumper statDump = new StatisticDumper(AgentConfiguration.getProfieFile());
		statDump.start();

		final MethodsMapDumper methodDump = new MethodsMapDumper(AgentConfiguration.getMethodsMapFile());
		methodDump.start();

		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					statDump.exit();
					methodDump.exit();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				try {
					methodDump.dumpRest();
					statDump.dumpRest();
				} catch (Throwable e) {
					System.err.println("Shutdown hook error: " + e.getMessage());
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