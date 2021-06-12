package com.zh.asm.chapter2.tool;

import com.zh.asm.MyClassVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 使用TraceClassVisitor，以获得关于实际所生成内容的一个可读轨迹
 */
public class TraceClassVisitorMain {

    public static void main(String[] args) throws IOException {
        ClassReader classReader = new ClassReader("com/zh/asm/TestService");
        ClassWriter cw = new ClassWriter(0);
        //PrintWriter printWriter = new PrintWriter("F://text.txt");
        PrintWriter printWriter = new PrintWriter(System.out);
        TraceClassVisitor cv = new TraceClassVisitor(cw, printWriter);

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
