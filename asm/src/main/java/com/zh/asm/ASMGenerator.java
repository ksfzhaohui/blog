package com.zh.asm;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ASMGenerator {

    public static void main(String[] args) throws IOException, InterruptedException {
        ClassReader classReader = new ClassReader("com/zh/asm/TestService");
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        //处理
        ClassVisitor classVisitor = new MyClassVisitor(classWriter);
        classReader.accept(classVisitor, 0);
        byte[] data = classWriter.toByteArray();

        Thread.sleep(1000000);
        //输出
        FileOutputStream fileOutputStream = new FileOutputStream(new File("F:/asm/TestService.class"));
        fileOutputStream.write(data);
        fileOutputStream.close();
        System.out.println("generator success");
    }
}
