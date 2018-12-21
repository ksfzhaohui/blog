package com.java8.c2;

import java.util.ArrayList;
import java.util.List;

public class Test1 {

	public static void main(String[] args) {
		List<Apple> inventory = new ArrayList<>();
		inventory.add(new Apple("green", 200));
		inventory.add(new Apple("red", 100));
		inventory.add(new Apple("black", 110));
		inventory.add(new Apple("blue", 300));

		System.out.println(filterGreenApples(inventory));
		System.out.println(filterHeavyApples(inventory));
	}

	public static List<Apple> filterGreenApples(List<Apple> inventory) {
		List<Apple> result = new ArrayList<>();
		for (Apple apple : inventory) {
			if ("green".equals(apple.getColor())) {
				result.add(apple);
			}
		}
		return result;
	}

	public static List<Apple> filterHeavyApples(List<Apple> inventory) {
		List<Apple> result = new ArrayList<>();
		for (Apple apple : inventory) {
			if (apple.getWeight() > 150) {
				result.add(apple);
			}
		}
		return result;
	}
}
