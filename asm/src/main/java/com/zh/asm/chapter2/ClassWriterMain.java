package com.zh.asm.chapter2;

import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.objectweb.asm.Opcodes.*;

public class ClassWriterMain {

    public static void main(String[] args) throws IOException {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(V1_5, ACC_PUBLIC + ACC_ABSTRACT + ACC_INTERFACE,
                "pkg/Comparable", null, "java/lang/Object",
                new String[]{"pkg/Mesurable"});
        cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "LESS",
                "I", null, new Integer(-1)).visitEnd();
        cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "EQUAL",
                "I", null, new Integer(0)).visitEnd();
        cw.visitField(ACC_PUBLIC + ACC_FINAL + ACC_STATIC, "GREATER",
                "I", null, new Integer(1)).visitEnd();
        cw.visitMethod(ACC_PUBLIC + ACC_ABSTRACT, "compareTo",
                "(Ljava/lang/Object;)I", null, null).visitEnd();
        cw.visitEnd();
        byte[] b = cw.toByteArray();

        //输出
        FileOutputStream fileOutputStream = new FileOutputStream(new File("F:/asm/Comparable.class"));
        fileOutputStream.write(b);
        fileOutputStream.close();
        System.out.println("generator success");
    }
}
