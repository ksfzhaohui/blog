package com.zh.asm.chapter2;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM4;

public class RemoveMethodAdapter extends ClassVisitor {
    private String mName;
    private String mDesc;

    public RemoveMethodAdapter(
            ClassVisitor cv, String mName, String mDesc) {
        super(ASM4, cv);
        this.mName = mName;
        this.mDesc = mDesc;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name,
                                     String desc, String signature, String[] exceptions) {
        if (name.equals(mName) && desc.equals(mDesc)) {
            // 不要委托至下一个访问器 -> 这样将移除该方法
            return null;
        }
        return cv.visitMethod(access, name, desc, signature, exceptions);
    }
}