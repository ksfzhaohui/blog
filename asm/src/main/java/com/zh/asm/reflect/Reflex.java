package com.zh.asm.reflect;

import java.lang.reflect.Method;

public class Reflex {

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 1; i++) {
            test2();
        }
    }

    public static void test() throws Exception {
        long startTime = System.currentTimeMillis();
        TestBean testBean = new TestBean(1, "zhaohui", 18);
        Method[] ms = TestBean.class.getDeclaredMethods();
        for (int i = 0; i < 1_0000_0000; i++) {
            ms[i & ms.length - 1].invoke(testBean);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Reflex time:" + (endTime - startTime) + "ms");
    }

    public static void test2() throws Exception {
        TestBean testBean = new TestBean(1, "zhaohui", 18);
        Method[] ms = TestBean.class.getDeclaredMethods();
        for (int i = 0; i < ms.length; i++) {
            System.out.println(ms[i].invoke(testBean));
        }
    }

}
