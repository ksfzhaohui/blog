package com.serialize;

public class Manual {

	public static void main(String[] args) {

	}

	public static void test() {
		Person person = new Person("10001", "zhaohui");
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 1_0000_0000; i++) {
			if (i % 2 == 0) {
				person.getId();
			} else {
				person.getName();
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Manual time:" + (endTime - startTime) + "ms");
	}
}
