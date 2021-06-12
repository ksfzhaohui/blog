package com.zh.asm.chapter3;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;

import static org.objectweb.asm.Opcodes.*;

public class AddTimerAdapter extends ClassVisitor {
    private String owner;
    private boolean isInterface;

    public AddTimerAdapter(ClassVisitor cv) {
        super(ASM4, cv);
    }

    @Override
    public void visit(int version, int access, String name,
                      String signature, String superName, String[] interfaces) {
        cv.visit(version, access, name, signature, superName, interfaces);
        owner = name;
        isInterface = (access & ACC_INTERFACE) != 0;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name,
                                     String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
                exceptions);
        if (!isInterface && mv != null && !name.equals("<init>")) {
            mv = new AddTimerMethodAdapter(mv);
        }
        return mv;
    }

    @Override
    public void visitEnd() {
        if (!isInterface) {
            FieldVisitor fv = cv.visitField(ACC_PUBLIC + ACC_STATIC, "timer",
                    "J", null, null);
            if (fv != null) {
                fv.visitEnd();
            }
        }
        cv.visitEnd();
    }


    class AddTimerMethodAdapter extends MethodVisitor {
        public AddTimerMethodAdapter(MethodVisitor mv) {
            super(ASM4, mv);
        }

        @Override
        public void visitCode() {
            mv.visitCode();
            mv.visitFieldInsn(GETSTATIC, owner, "timer", "J");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System",
                    "currentTimeMillis", "()J", false);
            mv.visitInsn(LSUB);
            mv.visitFieldInsn(PUTSTATIC, owner, "timer", "J");
        }

        @Override
        public void visitInsn(int opcode) {
            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                mv.visitFieldInsn(GETSTATIC, owner, "timer", "J");
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System",
                        "currentTimeMillis", "()J", false);
                mv.visitInsn(LADD);
                mv.visitFieldInsn(PUTSTATIC, owner, "timer", "J");
            }
            mv.visitInsn(opcode);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            mv.visitMaxs(maxStack + 4, maxLocals);
        }
    }

    /**
     * 扩展 LocalVariablesSorter
     */
    class AddTimerMethodAdapter4 extends LocalVariablesSorter {
        private int time;

        public AddTimerMethodAdapter4(int access, String desc,
                                      MethodVisitor mv) {
            super(ASM4, access, desc, mv);
        }

        @Override
        public void visitCode() {
            super.visitCode();
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System",
                    "currentTimeMillis", "()J");
            time = newLocal(Type.LONG_TYPE);
            mv.visitVarInsn(LSTORE, time);
        }

        @Override
        public void visitInsn(int opcode) {
            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System",
                        "currentTimeMillis", "()J");
                mv.visitVarInsn(LLOAD, time);
                mv.visitInsn(LSUB);
                mv.visitFieldInsn(GETSTATIC, owner, "timer", "J");
                mv.visitInsn(LADD);
                mv.visitFieldInsn(PUTSTATIC, owner, "timer", "J");
            }
            super.visitInsn(opcode);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack + 4, maxLocals);
        }
    }

    class AddTimerMethodAdapter5 extends MethodVisitor {
        public LocalVariablesSorter lvs;
        public AnalyzerAdapter aa;
        private int time;
        private int maxStack;

        public AddTimerMethodAdapter5(MethodVisitor mv) {
            super(ASM4, mv);
        }

        @Override
        public void visitCode() {
            mv.visitCode();
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System",
                    "currentTimeMillis", "()J");
            time = lvs.newLocal(Type.LONG_TYPE);
            mv.visitVarInsn(LSTORE, time);
            maxStack = 4;
        }

        @Override
        public void visitInsn(int opcode) {
            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System",
                        "currentTimeMillis", "()J");
                mv.visitVarInsn(LLOAD, time);
                mv.visitInsn(LSUB);
                mv.visitFieldInsn(GETSTATIC, owner, "timer", "J");
                mv.visitInsn(LADD);
                mv.visitFieldInsn(PUTSTATIC, owner, "timer", "J");
                maxStack = Math.max(aa.stack.size() + 4, maxStack);
            }
            mv.visitInsn(opcode);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            mv.visitMaxs(Math.max(this.maxStack, maxStack), maxLocals);
        }
    }

    class AddTimerMethodAdapter6 extends AdviceAdapter {
        public AddTimerMethodAdapter6(int access, String name, String desc,
                                      MethodVisitor mv) {
            super(ASM4, mv, access, name, desc);
        }

        @Override
        protected void onMethodEnter() {
            mv.visitFieldInsn(GETSTATIC, owner, "timer", "J");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System",
                    "currentTimeMillis", "()J");
            mv.visitInsn(LSUB);
            mv.visitFieldInsn(PUTSTATIC, owner, "timer", "J");
        }

        @Override
        protected void onMethodExit(int opcode) {
            mv.visitFieldInsn(GETSTATIC, owner, "timer", "J");
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System",
                    "currentTimeMillis", "()J");
            mv.visitInsn(LADD);
            mv.visitFieldInsn(PUTSTATIC, owner, "timer", "J");
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(maxStack + 4, maxLocals);
        }
    }

}
