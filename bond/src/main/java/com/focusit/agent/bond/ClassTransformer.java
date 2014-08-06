package com.focusit.agent.bond;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Properties;

/**
 * Class instrumentation toolkit
 * <p/>
 * Created by Denis V. Kirpichenkov on 06.08.14.
 */
public class ClassTransformer implements ClassFileTransformer {
	private ClassPool classPool;

	private final Properties properties;

	public ClassTransformer(Properties properties) {
		this.properties = properties;

		classPool = new ClassPool();
		classPool.appendSystemPath();
		try {
			classPool.appendPathList(System.getProperty("java.class.path"));

			// make sure that MetricReporter is loaded
//			classPool.get("com.chimpler.example.agentmetric.MetricReporter").getClass();
			classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] transform(ClassLoader loader, String fullyQualifiedClassName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		String className = fullyQualifiedClassName.replace("/", ".");
		classPool.appendClassPath(new ByteArrayClassPath(className, classfileBuffer));

		try {
			CtClass ctClass = classPool.get(className);
			if (ctClass.isFrozen()) {
//					logger.debug("Skip class {}: is frozen", className);
				return null;
			}

			if (ctClass.isPrimitive() || ctClass.isArray() || ctClass.isAnnotation()
				|| ctClass.isEnum() || ctClass.isInterface()) {
//					logger.debug("Skip class {}: not a class", className);
				return null;
			}
			boolean isClassModified = false;
			for (CtMethod method : ctClass.getDeclaredMethods()) {
				// if method is annotated, add the code to measure the time
//					if (method.hasAnnotation(Measured.class)) {
//						try {
//							if (method.getMethodInfo().getCodeAttribute() == null) {
//								logger.debug("Skip method " + method.getLongName());
//								continue;
//							}
//							logger.debug("Instrumenting method " + method.getLongName());
//							method.addLocalVariable("__metricStartTime", CtClass.longType);
//							method.insertBefore("__metricStartTime = System.currentTimeMillis();");
//							String metricName = ctClass.getName() + "." + method.getName();
//							method.insertAfter("com.chimpler.example.agentmetric.MetricReporter.reportTime(\""
//								+ metricName + "\", System.currentTimeMillis() - __metricStartTime);");
//							isClassModified = true;
//						} catch (Exception e) {
//							logger.warn("Skipping instrumentation of method {}: {}", method.getName(), e.getMessage());
//						}
//					}
			}
			if (isClassModified) {
				return ctClass.toBytecode();
			}
		} catch (Exception e) {
//				logger.debug("Skip class {}: ", className, e.getMessage());
		}
		return classfileBuffer;
	}
}
