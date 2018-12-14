package com.java8.c7;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Test43 {

	public static void main(String[] args) {
		List<String> title = Arrays.asList("Java8", "In", "Action");
		Stream<String> s = title.stream();
		s.forEach(System.out::println);
		// 流只能消费一次
		s.forEach(System.out::println);
	}

}
