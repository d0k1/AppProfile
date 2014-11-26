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
public class JavaAssistClassTransformer implements ClassFileTransformer {
	private ClassPool classPool;

	private final Properties properties;

	public JavaAssistClassTransformer(Properties properties) {
		this.properties = properties;

		classPool = new ClassPool();
		classPool.appendSystemPath();
		try {
			classPool.appendPathList(System.getProperty("java.class.path"));
			classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public byte[] transform(ClassLoader loader, String fullyQualifiedClassName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		String className = fullyQualifiedClassName.replace("/", ".");
		if(className.startsWith("com.focusit.agent.example"))
			System.err.println("transforming "+className);
		else {
			//System.err.println("skipping "+className);
			return classfileBuffer;
		}

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
				System.err.println(method.getLongName());
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
							String addMethod = "__metricMethodId = com.focusit.agent.bond.metrics.MethodsMap.getInstance().addMethod(\""+method.getLongName()+"\");";
							method.insertBefore(addMethod + getTime);

//							String metricName = ctClass.getName() + "." + method.getName();
							method.insertAfter("com.focusit.agent.bond.metrics.Statistics.storeData(__metricMethodId, __metricStartTime, com.focusit.agent.bond.time.GlobalTime.getCurrentTime());");
							isClassModified = true;
//						} catch (Exception e) {
//							logger.warn("Skipping instrumentation of method {}: {}", method.getName(), e.getMessage());
//						}
//					}
			}
			if (isClassModified) {
				return ctClass.toBytecode();
			}
		} catch (Throwable e) {
				System.err.println(e.getMessage());
//				logger.debug("Skip class {}: ", className, e.getMessage());
		}
		return classfileBuffer;
	}
}
