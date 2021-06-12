package com.zh.asm;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.LocalVariablesSorter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class TimeCountAdpter extends ClassVisitor implements Opcodes {
    private String owner;
    private boolean isInterface;

    public TimeCountAdpter(ClassVisitor classVisitor) {
        super(ASM6, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        owner = name;
        isInterface = (access & ACC_INTERFACE) != 0;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);

        if (!isInterface && mv != null && !name.equals("<init>")) {
            AddTimerMethodAdapter at = new AddTimerMethodAdapter(mv);
            at.analyzerAdapter = new AnalyzerAdapter(owner, access, name, descriptor, at);
            at.localVariablesSorter = new LocalVariablesSorter(access, descriptor, at.analyzerAdapter);

            return at.localVariablesSorter;
        }

        return mv;
    }

    public void visitEnd() {
        cv.visitEnd();
    }

    class AddTimerMethodAdapter extends MethodVisitor {
        private int time;
        private int maxStack;
        public LocalVariablesSorter localVariablesSorter;
        public AnalyzerAdapter analyzerAdapter;

        public AddTimerMethodAdapter(MethodVisitor methodVisitor) {
            super(ASM6, methodVisitor);
        }


        @Override
        public void visitCode() {
            mv.visitCode();
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
            time = localVariablesSorter.newLocal(Type.LONG_TYPE);
            mv.visitVarInsn(LSTORE, time);
            maxStack = 4;
        }

        @Override
        public void visitInsn(int opcode) {
            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {

                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
                mv.visitVarInsn(LLOAD, time);
                mv.visitInsn(LSUB);
                mv.visitVarInsn(LSTORE, time);

                mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                mv.visitVarInsn(LLOAD, time);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(J)V", false);
                maxStack = Math.max(analyzerAdapter.stack.size() + 4, maxStack);
            }
            mv.visitInsn(opcode);
        }

        @Override
        public void visitMaxs(int maxStack, int maxLocals) {
            super.visitMaxs(Math.max(maxStack, this.maxStack), maxLocals);
        }
    }

    public static void main(String[] args) throws IOException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, IOException {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
//        TraceClassVisitor tv = new TraceClassVisitor(cw, new PrintWriter(System.out));
        TimeCountAdpter addFiled = new TimeCountAdpter(cw);
        ClassReader classReader = new ClassReader("com/zh/asm/TestService");
        classReader.accept(addFiled, ClassReader.EXPAND_FRAMES);


        FileOutputStream fileOutputStream = new FileOutputStream(new File("F:/asm/TestService.class"));
        fileOutputStream.write(cw.toByteArray());
        fileOutputStream.close();
        System.out.println("generator success");

    }
}
