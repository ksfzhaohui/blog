package com.serialize;

public class Test {

	public static void main(String[] args) throws Throwable {
		for (int i = 0; i < 3; i++) {
			Manual.test();
			ASM.test();
			Reflex.test();
			ReflexMH.test();
			System.out.println("==============");
		}
	}
}
