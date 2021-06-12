package com.zh.asm.chapter2;

import org.objectweb.asm.*;

import static org.objectweb.asm.Opcodes.ASM4;

/**
 * 分析类
 * 分析一个已经存在的类时，惟一必需的组件是 ClassReader 组件
 */
public class ClassPrinter extends ClassVisitor {

    public ClassPrinter() {
        super(ASM4);
    }

    public void visit(int version, int access, String name,
                      String signature, String superName, String[] interfaces) {
        System.out.println(name + " extends " + superName + " {");
    }

    public void visitSource(String source, String debug) {
    }

    public void visitOuterClass(String owner, String name, String desc) {
    }

    public AnnotationVisitor visitAnnotation(String desc,
                                             boolean visible) {
        return null;
    }

    public void visitAttribute(Attribute attr) {
    }

    public void visitInnerClass(String name, String outerName,
                                String innerName, int access) {
    }

    public FieldVisitor visitField(int access, String name,
                                   String desc,
                                   String signature, Object value) {
        System.out.println(" " + desc + " " + name);
        return null;
    }

    public MethodVisitor visitMethod(int access, String name,
                                     String desc, String signature, String[] exceptions) {
        System.out.println(" " + name + desc);
        return null;
    }

    public void visitEnd() {
        System.out.println("}");
    }
}
