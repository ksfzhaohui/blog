package com.java8.c4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.java8.c2.Apple;

public class ListTest {

	public static void main(String[] args) {
		List<Apple> inventory = Arrays.asList(new Apple("green", 200), new Apple("red", 100), new Apple("black", 110),
				new Apple("blue", 300));
		List<Apple> redApples = filter(inventory, (Apple apple) -> "red".equals(apple.getColor()));
		System.out.println(redApples);

		List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
		List<Integer> evenNumbers = filter(numbers, (Integer i) -> i % 2 == 0);
		System.out.println(evenNumbers);
	}

	public static <T> List<T> filter(List<T> list, Predicate<T> p) {
		List<T> result = new ArrayList<>();
		for (T e : list) {
			if (p.test(e)) {
				result.add(e);
			}
		}
		return result;
	}

}
