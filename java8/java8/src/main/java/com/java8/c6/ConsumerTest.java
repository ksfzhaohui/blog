package com.java8.c6;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ConsumerTest {

	public static void main(String[] args) {
		forEach(Arrays.asList(1, 2, 3, 4, 5), (Integer i) -> System.out.println(i));

	}

	public static <T> void forEach(List<T> list, Consumer<T> c) {
		for (T i : list) {
			c.accept(i);
		}
	}
}
