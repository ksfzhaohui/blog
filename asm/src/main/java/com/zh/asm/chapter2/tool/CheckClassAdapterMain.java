package com.zh.asm.chapter2.tool;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 核实对其方法的调用顺序是否恰当，以及参数是否有效
 */
public class CheckClassAdapterMain {
    public static void main(String[] args) throws IOException {
        ClassReader classReader = new ClassReader("com/zh/asm/TestService");
        ClassWriter cw = new ClassWriter(0);
        PrintWriter printWriter = new PrintWriter(System.out);
        TraceClassVisitor tcv = new TraceClassVisitor(cw, printWriter);
        CheckClassAdapter cv = new CheckClassAdapter(tcv);

        //处理
        classReader.accept(cv, 0);
        byte[] data = cw.toByteArray();
        //输出
        FileOutputStream fileOutputStream = new FileOutputStream(new File("F:/asm/TestService.class"));
        fileOutputStream.write(data);
        fileOutputStream.close();
        System.out.println("generator success");
    }
}
