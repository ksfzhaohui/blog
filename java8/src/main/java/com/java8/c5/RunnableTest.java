package com.java8.c5;

public class RunnableTest {

	public static void main(String[] args) {

		Thread t = new Thread(new Runnable() {
			public void run() {
				System.out.println("Hello world");
			}
		});

		t.run();

		Thread tl = new Thread(() -> System.out.println("Hello world Lambd"));
		tl.run();
	}

}
