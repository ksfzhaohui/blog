package com.java8.c8;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Stream;

public class Test57 {

	public static void main(String[] args) {
		// 1.由值创建流
		Stream<String> stream = Stream.of("Java 8 ", "Lambdas ", "In ", "Action");
		stream.map(String::toUpperCase).forEach(System.out::println);

		// 空流
		Stream<String> emptyStream = Stream.empty();
		System.out.println(emptyStream);

		// 2.由数组创建流
		int[] numbers = { 2, 3, 5, 7, 11, 13 };
		int sum = Arrays.stream(numbers).sum();
		System.out.println(sum);

		// 3.由文件创建流
		long uniqueWords = 0;
		try (Stream<String> lines = Files.lines(Paths.get("data.txt"), Charset.defaultCharset())) {
			uniqueWords = lines.flatMap(line -> Arrays.stream(line.split(" "))).distinct().count();
		} catch (IOException e) {
		}
		System.out.println(uniqueWords);

		// 4.创建无限流
		Stream.iterate(0, n -> n + 2).limit(10).forEach(System.out::println);
		Stream.generate(Math::random).limit(5).forEach(System.out::println);
	}

}
