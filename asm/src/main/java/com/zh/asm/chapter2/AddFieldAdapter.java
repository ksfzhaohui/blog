package com.zh.asm.chapter2;

import com.zh.asm.MyClassVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * 增加类成员
 * <p>
 * visit visitSource? visitOuterClass?
 * ( visitAnnotation | visitAttribute )*
 * ( visitInnerClass | visitField | visitMethod )*
 * visitEnd
 */
public class AddFieldAdapter extends ClassVisitor {
    private int fAcc;
    private String fName;
    private String fDesc;
    private boolean isFieldPresent;

    public AddFieldAdapter(ClassVisitor cv, int fAcc, String fName,
                           String fDesc) {
        super(ASM4, cv);
        this.fAcc = fAcc;
        this.fName = fName;
        this.fDesc = fDesc;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc,
                                   String signature, Object value) {
        if (name.equals(fName)) {
            isFieldPresent = true;
        }
        return cv.visitField(access, name, desc, signature, value);
    }

    @Override
    public void visitEnd() {
        if (!isFieldPresent) {
            FieldVisitor fv = cv.visitField(fAcc, fName, fDesc, null, null);
            if (fv != null) {
                fv.visitEnd();
            }
        }
        cv.visitEnd();
    }

    public static void main(String[] args) throws IOException {
        ClassReader classReader = new ClassReader("com/zh/asm/TestService");
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        //处理
        ClassVisitor classVisitor = new AddFieldAdapter(classWriter,ACC_PUBLIC + ACC_FINAL + ACC_STATIC,"id","I");
        classReader.accept(classVisitor, 0);

        byte[] data = classWriter.toByteArray();
        //输出
        FileOutputStream fileOutputStream = new FileOutputStream(new File("F:/asm/TestService_Add.class"));
        fileOutputStream.write(data);
        fileOutputStream.close();
        System.out.println("generator success");
    }
}
