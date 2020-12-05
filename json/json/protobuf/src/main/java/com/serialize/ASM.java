package com.serialize;

import com.esotericsoftware.reflectasm.MethodAccess;

public class ASM {

	public static void main(String[] args) {

	}

	public static void test() {
		Person person = new Person("10001", "zhaohui");
		long startTime = System.currentTimeMillis();

		MethodAccess methodAccess = MethodAccess.get(Person.class);
		String[] mns = methodAccess.getMethodNames();
		int len = mns.length;
		int indexs[] = new int[len];
		for (int i = 0; i < len; i++) {
			indexs[i] = methodAccess.getIndex(mns[i]);
		}
		for (int i = 0; i < 1_0000_0000; i++) {
			methodAccess.invoke(person, indexs[i & len - 1]);
		}

		long endTime = System.currentTimeMillis();
		System.out.println("ASM time:" + (endTime - startTime) + "ms");
	}

}
