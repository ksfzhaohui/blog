package com.zh.asm.chapter2;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.objectweb.asm.Opcodes.ASM4;
import static org.objectweb.asm.Opcodes.V1_5;

/**
 * 转换类
 */
public class ChangeVersionAdapter extends ClassVisitor {
    public ChangeVersionAdapter(ClassVisitor cv) {
        super(ASM4, cv);
    }

    @Override
    public void visit(int version, int access, String name, String
            signature, String superName, String[] interfaces) {
        cv.visit(V1_5, access, name, signature, superName, interfaces);
    }

    public static void main(String[] args) throws IOException {
        long startTime = System.currentTimeMillis();
        ClassReader cr = new ClassReader("com/zh/asm/TestService");
        //ClassReader 和 ClassWriter 组件拥有对对方的引用
        ClassWriter cw = new ClassWriter(0);
//        ClassWriter cw = new ClassWriter(cr, 0);
        ChangeVersionAdapter ca = new ChangeVersionAdapter(cw);
        cr.accept(ca, 0);

        byte[] b2 = cw.toByteArray();
        long endTime = System.currentTimeMillis();
        System.out.println("cost time : " + (endTime - startTime));
        //输出
        FileOutputStream fileOutputStream = new FileOutputStream(new File("F:/asm/TestService.class"));
        fileOutputStream.write(b2);
        fileOutputStream.close();
        System.out.println("generator success");
    }
}