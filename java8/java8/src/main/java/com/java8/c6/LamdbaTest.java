package com.java8.c6;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LamdbaTest {

	public static void main(String[] args) throws IOException {

		System.out.println(processFile());
		/**
		 * 任何函数式接口都不允许抛出已检查异常(checked exception)；如果需要lamdba表达式来抛出异常 有两种方式：
		 * 1.定义一个自己的函数式接口，并声明已检查异常 2.把lamdba包在一个try/catch中
		 */
		System.out.println(processFile((BufferedReader br) -> {
			try {
				return br.readLine();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}));
		System.out.println(processFile((BufferedReader br) -> br.readLine() + br.readLine()));

	}

	public static String processFile() throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
			return br.readLine();
		}
	}

	public static String processFile(BufferedReaderProcessor p) throws IOException {
		try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
			return p.process(br);
		}
	}

}
