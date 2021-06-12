package com.zh.asm.reflect;


import com.esotericsoftware.reflectasm.FieldAccess;
import com.esotericsoftware.reflectasm.MethodAccess;
import com.zh.asm.reflect.extend.FieldAccessTest;
import com.zh.asm.reflect.extend.MethodAccessTest;

import java.io.IOException;
import java.lang.reflect.Field;

public class ReflectASM {

    public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException, IOException {
        for (int i = 0; i < 1; i++) {
            test3();
        }
    }

    public static void test() throws IOException {
        TestBean testBean = new TestBean(1, "zhaohui", 18);
        long startTime = System.currentTimeMillis();

        MethodAccess methodAccess = MethodAccess.get(TestBean.class);
        String[] mns = methodAccess.getMethodNames();
        int len = mns.length;
        int indexs[] = new int[len];
        for (int i = 0; i < len; i++) {
            indexs[i] = methodAccess.getIndex(mns[i]);
        }
        for (int i = 0; i < 1_0000_0000; i++) {
            System.out.println(methodAccess.invoke(testBean, indexs[i & len - 1]));
        }

        long endTime = System.currentTimeMillis();
        System.out.println("ASM time:" + (endTime - startTime) + "ms");
    }

    public static void test2() throws IOException, IllegalAccessException {
        TestBean testBean = new TestBean(1, "zhaohui", 18);
        MethodAccess methodAccess = MethodAccess.get(TestBean.class);
        String[] mns = methodAccess.getMethodNames();

        for (int i = 0; i < mns.length; i++) {
            System.out.println(methodAccess.invoke(testBean, mns[i]));
        }
    }

    public static void test3() throws IOException, IllegalAccessException {
        TestBean testBean = new TestBean(1, "zhaohui", 18);
        FieldAccess fieldAccess = FieldAccess.get(TestBean.class);
        Field[] fns = fieldAccess.getFields();

        for (int i = 0; i < fns.length; i++) {
            System.out.println(fns[i].get(testBean));
        }
    }

}
