package com.focusit.agent.bond;

import com.focusit.agent.metrics.MethodsMap;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

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

		if(AgentConfiguration.isClassExcluded(className))
			return classfileBuffer;

		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		ModifierClassWriter mcw = new ModifierClassWriter(Opcodes.ASM5, cw, className);
		ClassReader cr = new ClassReader(classfileBuffer);
		cr.accept(mcw, 0);
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
			return new ModifierMethodWriter(Opcodes.ASM5, mv, name, className, methodId);//new TryFinallyAdapter(Opcodes.ASM5, mv, access, name, desc, methodId);
		}

	}

	static class TryFinallyAdapter extends AdviceAdapter{
		private final String name;
		private final long methodId;
		protected TryFinallyAdapter(int api, MethodVisitor mv, int access, String name, String desc, long methodId) {
			super(api, mv, access, name, desc);
			this.name = name;
			this.methodId = methodId;
		}

		@Override
		protected void onMethodEnter() {
			super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			super.visitLdcInsn("Method " + methodDesc  + " Enter "+methodId);
			super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
		}

		@Override
		protected void onMethodExit(int opcode) {
			super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
			super.visitLdcInsn("Method " + methodDesc  + " Leave "+methodId);
			super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
		}
	}

	public static class ModifierMethodWriter extends MethodVisitor {

		// methodName to make sure adding try catch block for the specific
		// method.
		private final String methodName;
		private final String className;
		private final long methodId;

		// below label variables are for adding try/catch blocks in instrumented
		// code.
		private Label lTryBlockStart;
		private Label lTryBlockEnd;
		private Label lCatchBlockStart;
		private Label lCatchBlockEnd;

		/**
		 * constructor for accepting methodVisitor object and methodName
		 *  @param api : the ASM API version implemented by this visitor
		 * @param mv : MethodVisitor obj
		 * @param methodName : methodName to make sure adding try catch block for the specific method.
		 * @param methodId
		 */
		public ModifierMethodWriter(int api, MethodVisitor mv, String methodName, String className, long methodId) {
			super(api, mv);
			this.methodName = methodName;
			this.className = className;
			this.methodId = methodId;
		}

		// We want to add try/catch block for the entire code in the method
		// so adding the try/catch when the method is started visiting the code.
		@Override
		public void visitCode() {
			super.visitCode();

			lTryBlockStart = new Label();
			lTryBlockEnd = new Label();
			lCatchBlockStart = new Label();
			lCatchBlockEnd = new Label();

			visitLdcInsn(methodId);
			visitMethodInsn(Opcodes.INVOKESTATIC, "com/focusit/agent/metrics/Statistics", "storeEnter", "(J)V", false);

//			visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//			visitLdcInsn("Method " + methodName + " Enter " + methodId);
//			visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
//
			// set up try-catch block for RuntimeException
			visitTryCatchBlock(lTryBlockStart, lTryBlockEnd, lCatchBlockStart, null);

			// started the try block
			visitLabel(lTryBlockStart);
		}

		@Override
		public void visitInsn(int opcode) {
			switch(opcode)
			{
				case Opcodes.IRETURN:
				case Opcodes.LRETURN:
				case Opcodes.FRETURN:
				case Opcodes.DRETURN:
				case Opcodes.ARETURN:
				case Opcodes.RETURN:

					visitLdcInsn(methodId);
					visitMethodInsn(Opcodes.INVOKESTATIC, "com/focusit/agent/metrics/Statistics", "storeLeave", "(J)V", false);
//					super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//					super.visitLdcInsn("Method " + methodName  + " Leave "+methodId);
//					super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
					break;
			}
			super.visitInsn(opcode);
		}

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {

			visitLabel(lTryBlockEnd);


			visitJumpInsn(Opcodes.GOTO, lCatchBlockEnd);

			visitLabel(lCatchBlockStart);

			visitLdcInsn(methodId);
			visitMethodInsn(Opcodes.INVOKESTATIC, "com/focusit/agent/metrics/Statistics", "storeLeaveException", "(J)V", false);
//			super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//			super.visitLdcInsn("Method " + methodName  + " Leave "+methodId);
//			super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);

			visitInsn(Opcodes.ATHROW);

			visitLabel(lCatchBlockEnd);

			super.visitMaxs(maxStack, maxLocals);
		}

	}
}
