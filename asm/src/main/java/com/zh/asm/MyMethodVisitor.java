package com.zh.asm;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Modifier;

public class MyMethodVisitor extends MethodVisitor implements Opcodes {
    public MyMethodVisitor(MethodVisitor mv) {
        super(Opcodes.ASM4, mv);
    }

    @Override
    public void visitCode() {
        super.visitCode();
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitLdcInsn("start");
//        mv.visitVarInsn(ALOAD,1);
//        mv.visitIntInsn(ALOAD,1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }

    @Override
    public void visitInsn(int opcode) {
        System.out.println("opcode = "+opcode);
        if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)
                || opcode == Opcodes.ATHROW) {
            //方法在返回之前，打印"end"
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitLdcInsn("end");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        }
        mv.visitInsn(opcode);
    }
}