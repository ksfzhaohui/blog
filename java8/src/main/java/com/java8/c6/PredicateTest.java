package com.java8.c6;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import com.java8.c2.Apple;

public class PredicateTest {

	public static void main(String[] args) {

		List<String> list = Arrays.asList("11", null, "33", "44");

		System.out.println(filter(list, (String s) -> !s.isEmpty()));

		List<Apple> inventory = Arrays.asList(new Apple("green", 200), new Apple("red", 100), new Apple("black", 110),
				new Apple("blue", 300));

		System.out.println(filter(inventory, (Apple a) -> a.getWeight() > 100));

	}

	public static <T> List<T> filter(List<T> list, Predicate<T> p) {
		List<T> results = new ArrayList<>();
		for (T s : list) {
			if (p.test(s)) {
				results.add(s);
			}
		}
		return results;
	}

}
