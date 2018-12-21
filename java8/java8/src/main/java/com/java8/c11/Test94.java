package com.java8.c11;

/**
 * 1.类中的方法优先级最高；类或父类中声明的方法优先级高于任何声明为默认方法的优先级；
 * 2.如果第一条无法判定，那么子接口的优先级更高；优先选择拥有最具体实现的默认方法的接口，即如果B继承了，那么B就比A更加具体
 * 3.如果还是不行，继承多个接口的类必须通过显示覆盖和调用期望的方法，显示使用哪一个默认方法的实现；
 * @author hui.zhao.cfs
 *
 */
public class Test94 extends D implements B, A {
	public static void main(String... args) {
		new Test94().hello();
	}
}

interface A {
	default void hello() {
		System.out.println("Hello from A");
	}
}

interface B extends A {
	default void hello() {
		System.out.println("Hello from B");
	}
}

class D implements A {
	//@Override
	//public void hello() {
	//	System.out.println("Hello from D");
	//}
}