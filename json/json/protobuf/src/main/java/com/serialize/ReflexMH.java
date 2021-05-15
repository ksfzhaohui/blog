package com.serialize;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

import com.protostuff.Person;

public class ReflexMH {

	public static void main(String[] args) throws Throwable {
		for (int i = 0; i < 2; i++) {
			test();
		}
	}

	public static void test() throws Throwable {
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
			mhs[i & len - 1].invoke(person);
		}
		long endTime = System.currentTimeMillis();
		System.out.println("ReflexMH time:" + (endTime - startTime) + "ms");
	}
	
	public static void test2() throws Throwable {
		Person person = new Person("10001", "zhaohui");
		Lookup lookup = MethodHandles.lookup();
//		Method[] ms = Person.class.getDeclaredMethods();
//		int len = ms.length;
		MethodHandle[] mhs = new MethodHandle[2];
		
		 MethodType methodType1 = MethodType.methodType(int.class);
		 MethodType methodType2 = MethodType.methodType(String.class);
		 MethodHandle getId = lookup.findVirtual(Person.class, "getId", methodType1);
		 MethodHandle getName = lookup.findVirtual(Person.class, "getName", methodType2);
		 mhs[0]=getId;
		 mhs[1]=getName;
		 
//		for (int i = 0; i < len; i++) {
//			mhs[i] = lookup.unreflect(ms[i]);
//		}
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 1_0000_0000; i++) {
			mhs[i & 1].invoke(person);
//			System.out.println(mhs[i & len - 1].invoke(person));
		}
		long endTime = System.currentTimeMillis();
		System.out.println("ReflexMH time:" + (endTime - startTime) + "ms");
	}
}
