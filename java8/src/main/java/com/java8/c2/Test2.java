package com.java8.c2;

import java.util.ArrayList;
import java.util.List;

public class Test2 {

	public static void main(String[] args) {
		List<Apple> inventory = new ArrayList<>();
		inventory.add(new Apple("green", 200));
		inventory.add(new Apple("red", 100));
		inventory.add(new Apple("black", 110));
		inventory.add(new Apple("blue", 300));

		System.out.println(filterApples(inventory, Apple::isGreenApple));
		System.out.println(filterApples(inventory, Apple::isHeavyApple));
	}

	static List<Apple> filterApples(List<Apple> inventory, Predicate<Apple> p) {
		List<Apple> result = new ArrayList<>();
		for (Apple apple : inventory) {
			if (p.test(apple)) {
				result.add(apple);
			}
		}
		return result;
	}
}
