package com.focusit.agent.bond;

import com.focusit.agent.metrics.MethodsMap;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import javassist.expr.NewArray;
import javassist.expr.NewExpr;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;

/**
 * Java bytecode instrumentation based on javaassist library
 *
 * Created by Denis V. Kirpichenkov on 06.08.14.
 */
public class JavaAssistClassTransformer implements ClassFileTransformer {
	private final Instrumentation instrumentation;
	private ClassPool classPool;

	private List<WeakReference<ClassLoader>> loaders = new ArrayList<>();

	public JavaAssistClassTransformer(Instrumentation instrumentation) {
		this.instrumentation = instrumentation;

		classPool = ClassPool.getDefault();

		String jars = System.getProperty("agent.search.classpath");

		if (jars != null) {
			String paths[] = jars.split(",");
			for (String jar : paths) {
				try {
					classPool.appendClassPath(jar);
				} catch (NotFoundException e) {
					System.err.println("Error adding classpath by property: " + e.getMessage());
				}
			}
		}
	}

	private boolean modifyBehavoiur(CtBehavior method) throws CannotCompileException {
		// skip empty and abstract methods
		if (method.isEmpty())
			return false;

		String methodName = method.getLongName();
		long methodId = MethodsMap.addMethod(methodName);
		//LOG.finer(String.format("Instrumenting method %s with index %s", methodName, methodId));

		String before = "com.focusit.agent.metrics.Statistics.storeEnter("+ methodId + "L);";
		String after = "com.focusit.agent.metrics.Statistics.storeLeave("+ methodId + "L);";
		method.insertBefore(before);
		method.insertAfter(after, true);

		method.instrument(new ExprEditor(){
			@Override
			public void edit(NewArray a) throws CannotCompileException {
				super.edit(a);
			}

			@Override
			public void edit(NewExpr e) throws CannotCompileException {
				super.edit(e);
			}

			@Override
			public void edit(MethodCall m) throws CannotCompileException {
				super.edit(m);
			}
		});

		return true;
	}

	@Override
	public byte[] transform(ClassLoader loader, String fullyQualifiedClassName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		try {
			processClassloader((URLClassLoader) loader);
		} catch (NotFoundException e) {
			System.err.println("Error processing classloader: " + e.getMessage());
		}

		String className = fullyQualifiedClassName.replace("/", ".");

		if(AgentConfiguration.isClassExcluded(className))
			return classfileBuffer;

		//LOG.finer("Instrumenting "+className);

//		System.out.println("Instrumenting "+className);

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

			for(CtConstructor constructor:ctClass.getConstructors()){
				if(modifyBehavoiur(constructor)) {
					constructor.getMethodInfo().rebuildStackMapIf6(classPool, ctClass.getClassFile());
					isClassModified = true;
				}
			}

			for (CtMethod method : ctClass.getDeclaredMethods()) {
				if(modifyBehavoiur(method)) {
					method.getMethodInfo().rebuildStackMapIf6(classPool, ctClass.getClassFile());
					isClassModified = true;
				}
			}

			if (isClassModified) {
				ctClass.detach();
				return ctClass.toBytecode();
			}
		} catch (Throwable e) {
			System.err.println("Instrumentation method " + methodName + " error: " + e.getMessage());
		}
		return classfileBuffer;
	}

	private void processClassloader(URLClassLoader loader) throws NotFoundException {
		List<String> jars = getClassloaderURLs(loader);

		//LOG.finer("Adding "+jars.size()+" urls to class pool");

		for(String jar:jars){
			classPool.appendClassPath(jar);
		}
	}

	private List<String> getClassloaderURLs(URLClassLoader loader) throws NotFoundException {

		if (loader == null)
		{
			return new ArrayList<>();
		}

		for(WeakReference<ClassLoader> ref:loaders){
			if(ref.get()!=null && ref.get().equals(loader)){
				return new ArrayList<>();
			}
		}

		loaders.add(new WeakReference<ClassLoader>(loader));

		List<String> jars = new ArrayList<>();
		for (URL url : loader.getURLs()) {
			jars.add(url.getFile());
		}

		jars.addAll(getClassloaderURLs((URLClassLoader) loader.getParent()));

		return jars;
	}
}
