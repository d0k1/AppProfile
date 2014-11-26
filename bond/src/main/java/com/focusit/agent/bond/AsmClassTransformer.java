package com.focusit.agent.bond;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Properties;

/**
 * Created by Denis V. Kirpichenkov on 25.11.14.
 */
public class AsmClassTransformer implements ClassFileTransformer {

	private final Properties properties;

	public AsmClassTransformer(Properties properties) {
		this.properties = properties;
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		ClassWriter cw = new ClassWriter(0);
		ClassReader cr = new ClassReader(classfileBuffer);
		cr.accept(cw, 0);
		return cw.toByteArray();
	}
}
