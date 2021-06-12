package com.zh.asm.chapter3;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM4;

public class RemoveNopClassAdapter extends ClassVisitor {
    public RemoveNopClassAdapter(ClassVisitor cv) {
        super(ASM4, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name,
                                     String desc, String signature, String[] exceptions) {
        MethodVisitor mv;
        //构造了一个类似于类适配器链的方法适配器链
        mv = cv.visitMethod(access, name, desc, signature, exceptions);
        if (mv != null) {
            mv = new RemoveNopAdapter(mv);
        }
        return mv;
    }
}
