package com.java8.c7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.groupingBy;

public class Test41 {

	public static void main(String[] args) {
		// 1.普通模式
		List<Dish> menu = Arrays.asList(new Dish("苹果", 100, Type.FRUIT), new Dish("猪肉", 200, Type.MEAT),
				new Dish("蛋糕", 150, Type.OTHER));
		List<Dish> lowCaloricDishes = new ArrayList<>();
		for (Dish d : menu) {
			if (d.getCalories() < 400) {
				lowCaloricDishes.add(d);
			}
		}
		Collections.sort(lowCaloricDishes, new Comparator<Dish>() {
			public int compare(Dish d1, Dish d2) {
				return Integer.compare(d1.getCalories(), d2.getCalories());
			}
		});
		List<String> lowCaloricDishesName1 = new ArrayList<>();
		for (Dish d : lowCaloricDishes) {
			lowCaloricDishesName1.add(d.getName());
		}
		System.out.println(lowCaloricDishesName1);

		// 2.流模式
		List<String> lowCaloricDishesName2 = menu.stream().filter(d -> d.getCalories() < 400)
				.sorted(comparing(Dish::getCalories)).map(a -> a.getName()).collect(toList());
		System.out.println(lowCaloricDishesName2);

		// 3.流模式
		List<String> lowCaloricDishesName3 = menu.parallelStream().filter(d -> d.getCalories() < 400)
				.sorted(comparing(Dish::getCalories)).map(a -> a.getName()).collect(toList());
		System.out.println(lowCaloricDishesName3);

		// 4.分组
		Map<Type, List<Dish>> dishesByType = menu.stream().collect(groupingBy(Dish::getType));
		System.out.println(dishesByType);
	}

}

enum Type {
	MEAT, FRUIT, OTHER;
}

class Dish {
	private int calories;
	private String name;
	private Type type;

	public Dish(String name, int calories, Type type) {
		this.name = name;
		this.calories = calories;
		this.type = type;
	}

	public int getCalories() {
		return calories;
	}

	public void setCalories(int calories) {
		this.calories = calories;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

}
