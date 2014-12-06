package com.focusit.agent.bond;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * Created by Denis V. Kirpichenkov on 25.11.14.
 */
public class AsmClassTransformer implements ClassFileTransformer {

	public AsmClassTransformer(String excludes[], String ignoreExcludes[], Instrumentation instrumentation) {
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		ClassWriter cw = new ClassWriter(0);
		ClassReader cr = new ClassReader(classfileBuffer);
		cr.accept(cw, 0);
		return cw.toByteArray();
	}
}
