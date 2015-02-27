package com.focusit.agent.bond;

import com.focusit.agent.metrics.MethodsMap;
import org.objectweb.asm.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

/**
 * Class transformer based on asm
 * Created by Denis V. Kirpichenkov on 25.11.14.
 */
public class AsmClassTransformer implements ClassFileTransformer {

	public AsmClassTransformer(Instrumentation instrumentation) {
	}

	@Override
	public byte[] transform(ClassLoader loader, String fullyQualifiedClassName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {

		String className = fullyQualifiedClassName.replace("/", ".");

		if(!className.equalsIgnoreCase("java.lang.Thread") && AgentConfiguration.isClassExcluded(className)) {
			return classfileBuffer;
		}

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

		ModifierClassWriter mcw = new ModifierClassWriter(Opcodes.ASM5, cw, className);
		ClassReader cr = new ClassReader(classfileBuffer);
		cr.accept(mcw, ClassReader.EXPAND_FRAMES);

		return cw.toByteArray();
	}

	static class ModifierClassWriter extends ClassVisitor {

		private final String className;
		private final StringBuilder builder;

		public ModifierClassWriter(int api, ClassWriter cv, String className) {
			super(api, cv);
			this.className = className;
			this.builder = new StringBuilder();
		}

		@Override
		public MethodVisitor visitMethod(int access, String name, String desc,
		                                 String signature, String[] exceptions) {

			MethodVisitor mv = super.visitMethod(access, name, desc, signature,
				exceptions);

			builder.append(className).append(".").append(name).append(desc);
			long methodId = MethodsMap.addMethod(builder.toString());
			builder.setLength(0);
			return new ModifierMethodWriter(Opcodes.ASM5, mv, name, className, methodId, desc);
		}

	}

	static class ModifierMethodWriter extends MethodVisitor implements Opcodes {

		private final String methodName;
		private final String className;
		private final long methodId;
		private final String methodDesc;

		private Label startFinally = new Label();


		public ModifierMethodWriter(int api, MethodVisitor mv, String methodName, String className, long methodId, String methodDesc) {
			super(api, mv);
			this.methodName = methodName;
			this.className = className;
			this.methodId = methodId;
			this.methodDesc = methodDesc;
		}


		@Override
		public void visitCode() {
			super.visitCode();

			onTry();

			mv.visitLabel(startFinally);
		}

		protected void onMethodExit(int opcode) {
			if(opcode!=ATHROW) {
				onFinally(opcode);
			}
		}

		@Override
		public void visitInsn(int opcode) {
			switch(opcode)
			{
				case RETURN:
				case IRETURN:
				case FRETURN:
				case ARETURN:
				case LRETURN:
				case DRETURN:
				case ATHROW:
					onMethodExit(opcode);
					break;
			}
			super.visitInsn(opcode);
		}

		private void onTry(){
			mv.visitLdcInsn(methodId);
			mv.visitMethodInsn(INVOKESTATIC, "com/focusit/agent/metrics/Statistics", "storeEnter", "(J)V", false);
		}

		private void onFinally(int opcode) {
			mv.visitLdcInsn(methodId);
			if(opcode==ATHROW){
				mv.visitMethodInsn(INVOKESTATIC, "com/focusit/agent/metrics/Statistics", "storeLeaveException", "(J)V", false);
			} else {
				mv.visitMethodInsn(INVOKESTATIC, "com/focusit/agent/metrics/Statistics", "storeLeave", "(J)V", false);
			}
		}

		private void printExit(){
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitLdcInsn("Method " + className + "." + methodName + " Leave " + methodId);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
		}

		private void printEnter(){
			mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			mv.visitLdcInsn("Method " + className+"."+methodName + " Enter " + methodId);
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
		}
		@Override
		public void visitMaxs(int maxStack, int maxLocals) {

			Label endFinally = new Label();
			mv.visitTryCatchBlock(startFinally,
				endFinally, endFinally, null);
			mv.visitLabel(endFinally);
			onFinally(Opcodes.ATHROW);
			mv.visitInsn(Opcodes.ATHROW);
			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
			mv.visitInsn(RETURN);
			mv.visitMaxs(maxStack, maxLocals);
		}

	}
}
