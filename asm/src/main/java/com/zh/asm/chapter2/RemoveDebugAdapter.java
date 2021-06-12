package com.zh.asm.chapter2;

import org.objectweb.asm.ClassVisitor;

import static org.objectweb.asm.Opcodes.ASM4;

/**
 * 移除类成员
 * 在适当的访问方法中不转发任何内容
 */
public class RemoveDebugAdapter extends ClassVisitor {
    public RemoveDebugAdapter(ClassVisitor cv) {
        super(ASM4, cv);
    }

    @Override
    public void visitSource(String source, String debug) {
    }

    @Override
    public void visitOuterClass(String owner, String name, String desc) {
    }

    @Override
    public void visitInnerClass(String name, String outerName,
                                String innerName, int access) {
    }
}
