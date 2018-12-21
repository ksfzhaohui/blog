package com.java8.c5;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.java8.c2.Apple;

public class ComparatorTest {

	public static void main(String[] args) {
		List<Apple> inventory = Arrays.asList(new Apple("green", 200), new Apple("red", 100), new Apple("black", 110),
				new Apple("blue", 300));

		inventory.sort(new Comparator<Apple>() {
			public int compare(Apple a1, Apple a2) {
				return a1.getWeight().compareTo(a2.getWeight());
			}
		});

		inventory.sort((Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight()));
		System.out.println(inventory);
	}
}
