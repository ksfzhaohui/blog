package com.zh.javassist;

import com.zh.asm.TestService;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class JavassistTest {

    public static void main(String[] args) throws Exception {
        ClassPool cp = ClassPool.getDefault();
        CtClass cc = cp.get("com.zh.asm.TestService");
        CtMethod m = cc.getDeclaredMethod("query");
        m.insertBefore("{ System.out.println(\"start\"); }");
        m.insertAfter("{ System.out.println(\"end\"); }");

        Class c = cc.toClass();
        Thread.sleep(1000000);
        cc.writeFile("F:/asm/TestService_javassist");
        TestService h = (TestService) c.newInstance();
        h.query();
    }
}
