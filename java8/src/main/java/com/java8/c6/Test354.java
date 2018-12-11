package com.java8.c6;

public class Test354 {

	/**
	 * 局部变量必须声明为final或事实上是final
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int portNumber = 1337;
		Runnable r = () -> System.out.println(portNumber);
		// portNumber=111;
		r.run();
	}

}
