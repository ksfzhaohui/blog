package com.zh.asm.chapter3;

import com.zh.asm.MyClassVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class AddTimerAdapterMain {

    public static void main(String[] args) throws IOException {
        ClassReader classReader = new ClassReader("com/zh/asm/TestService");
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        //处理
        ClassVisitor classVisitor = new AddTimerAdapter(classWriter);
        classReader.accept(classVisitor, ClassReader.SKIP_DEBUG);
        byte[] data = classWriter.toByteArray();
        //输出
        FileOutputStream fileOutputStream = new FileOutputStream(new File("F:/asm/TestService_AddTimer.class"));
        fileOutputStream.write(data);
        fileOutputStream.close();
        System.out.println("generator success");
    }
}
