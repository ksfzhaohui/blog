package com.zh.asm.chapter3;

import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ASM4;
import static org.objectweb.asm.Opcodes.NOP;

/**
 * 删除方法中的 NOP指令
 */
public class RemoveNopAdapter extends MethodVisitor {
    public RemoveNopAdapter(MethodVisitor mv) {
        super(ASM4, mv);
    }

    @Override
    public void visitInsn(int opcode) {
        //删除方法中的 NOP指令
        if (opcode != NOP) {
            mv.visitInsn(opcode);
        }
    }
}
