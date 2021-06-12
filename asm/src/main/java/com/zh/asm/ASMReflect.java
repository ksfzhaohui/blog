package com.zh.asm;

import org.objectweb.asm.ClassReader;

import java.io.IOException;

public class ASMReflect {

    public static void main(String[] args) throws IOException {
        ClassReader reader = new ClassReader("com/zh/asm/TestBean");
    }
}
