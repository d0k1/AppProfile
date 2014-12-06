package com.focusit.agent.bond;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * Class instrumentation toolkit
 * <p/>
 * Created by Denis V. Kirpichenkov on 06.08.14.
 */
public class JavaAssistClassTransformer implements ClassFileTransformer {
	private final String excludes[];
	private final String ignoreExcludes[];
	private final Instrumentation instrumentation;
	private ClassPool classPool;

	public JavaAssistClassTransformer(String excludes[], String ignoreExcludes[], Instrumentation instrumentation) {
		this.excludes = excludes;
		this.ignoreExcludes = ignoreExcludes;
		this.instrumentation = instrumentation;

		classPool = ClassPool.getDefault();
		try {
//			classPool.appendPathList(System.getProperty("java.class.path"));
//			classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] transform(ClassLoader loader, String fullyQualifiedClassName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		String className = fullyQualifiedClassName.replace("/", ".");
//		System.out.println("Check to transform: " + className);
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
//				System.out.println("Skipped: " + className);
				return classfileBuffer;
			} else {
//				if (loader != null)
//					System.err.println("Transforming " + className + " loaded by " + loader.toString());
//				else {
//					System.err.println("Transforming " + className + " and set loader");
//				}
			}
		}

		try {

			CtClass ctClass = classPool.makeClass(new java.io.ByteArrayInputStream(classfileBuffer));
//			CtClass ctClass = classPool.get(className);
			if (ctClass.isFrozen()) {
				return classfileBuffer;
			}

			if (ctClass.isPrimitive() || ctClass.isArray() || ctClass.isAnnotation()
				|| ctClass.isEnum() || ctClass.isInterface()) {
//					logger.debug("Skip class {}: not a class", className);
				return classfileBuffer;
			}
			boolean isClassModified = false;

			for (CtMethod method : ctClass.getDeclaredMethods()) {
//				System.err.println(method.getLongName());
				// if method is annotated, add the code to measure the time
//					if (method.hasAnnotation(Measured.class)) {
//						try {
//							if (method.getMethodInfo().getCodeAttribute() == null) {
//								logger.debug("Skip method " + method.getLongName());
//								continue;
//							}
				System.err.println("Instrumenting method " + method.getLongName());
				method.addLocalVariable("__metricStartTime", CtClass.longType);
				method.addLocalVariable("__metricMethodId", CtClass.longType);
				String getTime = "__metricStartTime = com.focusit.agent.bond.time.GlobalTime.getCurrentTime();";
				String addMethod = "__metricMethodId = com.focusit.utils.metrics.MethodsMap.getInstance().addMethod(\"" + method.getLongName() + "\");";
				method.insertBefore(addMethod + getTime);
//							method.insertBefore("System.err.println(\"Begin\");");
//							String metricName = ctClass.getName() + "." + method.getName();
				method.insertAfter("com.focusit.utils.metrics.Statistics.storeData(__metricMethodId, __metricStartTime, com.focusit.agent.bond.time.GlobalTime.getCurrentTime());");
				isClassModified = true;
//						} catch (Exception e) {
//							logger.warn("Skipping instrumentation of method {}: {}", method.getName(), e.getMessage());
//						}
//					}
			}

			if (isClassModified) {
//					classPool.importPackage("com.focusit.agent.bond.time");
//					classPool.importPackage("com.focusit.utils.metrics");
				ctClass.detach();
				byte klass[] = ctClass.toBytecode();
				return klass;
			}
		} catch (Throwable e) {
			System.err.println(e.getMessage());
//				logger.debug("Skip class {}: ", className, e.getMessage());
		}
		return classfileBuffer;
	}
}
