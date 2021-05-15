package com.serialize;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.protostuff.Person;

public class Bench1 {

	long v;

	public void func0() {
		v++;
	}

	public void func1() {
		v--;
	}

	public void func2() {
		v++;
	}

	public void func3() {
		v--;
	}

	public void testInterface() {
		long t = System.nanoTime();
		Runnable[] rs = { this::func0, this::func1, };//this::func2, this::func3, };
		for (int i = 0; i < 1_0000_0000; i++)
			rs[i & 1].run(); // 关键调用
		t = (System.nanoTime() - t) / 1_000_000;
		System.out.format("testInterface: %d %dms\n", v, t);
	}

	public void testReflect() throws Exception {
		long t = System.nanoTime();
		Method[] ms = { Bench1.class.getMethod("func0"), Bench1.class.getMethod("func1")};
				//Bench1.class.getMethod("func2"), Bench1.class.getMethod("func3"), };
		for (int i = 0; i < 1_0000_0000; i++)
			ms[i & 1].invoke(this); // 关键调用
		t = (System.nanoTime() - t) / 1_000_000;
		System.out.format("testReflect  : %d %dms\n", v, t);
	}
	
	public void testReflectASM() throws Exception {
		long t = System.nanoTime();
		MethodAccess access = MethodAccess.get(Bench1.class);
		int[] ms = { access.getIndex("func0"), access.getIndex("func1"),};
				//access.getIndex("func2"), access.getIndex("func3"), };
		for (int i = 0; i < 1_0000_0000; i++)
			access.invoke(this, ms[i & 1]);// 关键调用
		t = (System.nanoTime() - t) / 1_000_000;
		System.out.format("testReflectASM  : %d %dms\n", v, t);
	}
	
	public void testMethodHandle() throws Throwable {
        long t = System.nanoTime();
        Lookup lookup = MethodHandles.lookup();
        MethodHandle[] ms = {
            lookup.unreflect(Bench1.class.getMethod("func0")),
            lookup.unreflect(Bench1.class.getMethod("func1")),
//            lookup.unreflect(Bench1.class.getMethod("func2")),
//            lookup.unreflect(Bench1.class.getMethod("func3")),
        };
        for(int i = 0; i < 1_0000_0000; i++)
            ms[i & 1].invoke(this);
        t = (System.nanoTime() - t) / 1_000_000;
        System.out.format("testMethodHandle  : %d %dms\n", v, t);
    }
	
	private static void test() throws Throwable {
		Person person = new Person("10001", "zhaohui");
		Lookup lookup = MethodHandles.lookup();
		Method[] ms = Person.class.getDeclaredMethods();
		int len = ms.length;
		MethodHandle[] mhs = new MethodHandle[len];
		for (int i = 0; i < len; i++) {
			mhs[i] = lookup.unreflect(ms[i]);
		}
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 1_0000_0000; i++) {
			mhs[i & (len - 1)].invoke(person);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("MethodHandle time:" + (endTime - startTime) + "ms");
	}
	
	private static void testf() throws Exception {
		Person person = new Person("10001", "zhaohui");
		long startTime = System.currentTimeMillis();
		Method[] ms = Person.class.getDeclaredMethods();
		for (int i = 0; i < 1_0000_0000; i++) {
			ms[i & ms.length - 1].invoke(person);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Method time:" + (endTime - startTime) + "ms");
	}

	public static void main(String[] args) throws Throwable {
		Bench1 b;
		b = new Bench1(); // 预热部分
		b.testInterface();
		b = new Bench1();
		b.testReflect();
//		b = new Bench1();
//		b.testReflectASM();
//		b = new Bench1();
//		b.testMethodHandle();
		testf();
		test();
System.out.println("===================");
		b = new Bench1(); // 实测部分
		b.testInterface();
		b = new Bench1();
		b.testReflect();
//		b = new Bench1();
//		b.testReflectASM();
//		b = new Bench1();
//		b.testMethodHandle();
		testf();
		test();
	}
}
