package com.focusit.agent.bond;

import com.focusit.agent.metrics.MethodsMap;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.logging.Logger;

/**
 * Java bytecode instrumentation based on javaassist library
 *
 * Created by Denis V. Kirpichenkov on 06.08.14.
 */
public class JavaAssistClassTransformer implements ClassFileTransformer {
	private static final Logger LOG = Logger.getLogger(JavaAssistClassTransformer.class.getName());
	private final String excludes[];
	private final String ignoreExcludes[];
	private final Instrumentation instrumentation;
	private ClassPool classPool;

	public JavaAssistClassTransformer(String excludes[], String ignoreExcludes[], Instrumentation instrumentation) {
		this.excludes = excludes;
		this.ignoreExcludes = ignoreExcludes;
		this.instrumentation = instrumentation;

		classPool = ClassPool.getDefault();

		String jars = System.getProperty("agent.search.classpath");

		if (jars != null) {
			String paths[] = jars.split(",");
			for (String jar : paths) {
				try {
					classPool.appendClassPath(jar);
				} catch (NotFoundException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public byte[] transform(ClassLoader loader, String fullyQualifiedClassName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		String className = fullyQualifiedClassName.replace("/", ".");
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
				return classfileBuffer;
			}
		}

		String methodName = "";
		try {

			CtClass ctClass = classPool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
			if (ctClass.isFrozen()) {
				return classfileBuffer;
			}

			if (ctClass.isPrimitive() || ctClass.isArray() || ctClass.isAnnotation()
				|| ctClass.isEnum() || ctClass.isInterface()) {
				return classfileBuffer;
			}
			boolean isClassModified = false;

			for (CtMethod method : ctClass.getDeclaredMethods()) {

				// skip empty and abstract methods
				if (method.isEmpty())
					continue;

				methodName = method.getLongName();
				long methodId = MethodsMap.getInstance().addMethod(methodName);
				LOG.finer(String.format("Instrumenting method %s with index %s", methodName, methodId));

				method.addLocalVariable("__metricStartTime", CtClass.longType);
				String getTime = "__metricStartTime = com.focusit.agent.bond.time.GlobalTime.getCurrentTime();";
				method.insertBefore(getTime);
				method.insertAfter("com.focusit.agent.metrics.Statistics.storeData(" + methodId + "L, __metricStartTime, com.focusit.agent.bond.time.GlobalTime.getCurrentTime());");
				isClassModified = true;
			}

			if (isClassModified) {
				ctClass.detach();
				return ctClass.toBytecode();
			}
		} catch (Throwable e) {
			LOG.severe("Instrumentation method " + methodName + " error: " + e.getMessage());
		}
		return classfileBuffer;
	}
}
