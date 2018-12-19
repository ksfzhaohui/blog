package com.java8.c11;

/**
 * 菱形问题
 * 
 * @author hui.zhao.cfs
 *
 */
public class Test944 implements B4, C4 {
	public static void main(String... args) {
		new Test944().hello();
	}
}

interface A4 {
	default void hello() {
		System.out.println("Hello from A");
	}
}

interface B4 extends A4 {
}

interface C4 extends A4 {
}
