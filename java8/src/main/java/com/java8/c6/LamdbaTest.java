package com.java8.c6;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LamdbaTest {

	public static void main(String[] args) throws IOException {

		System.out.println(processFile());
		System.out.println(processFile((BufferedReader br) -> br.readLine()));
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
