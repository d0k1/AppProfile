package com.focusit.agent.bond;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * CGLib based class transformer
 *
 * Created by Denis V. Kirpichenkov on 06.12.14.
 */
class CGLibClassTransformer implements ClassFileTransformer {

	public CGLibClassTransformer(String excludes[], String ignoreExcludes[], Instrumentation instrumentation) {
	}

	@Override
	public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
		return classfileBuffer;
	}
}
