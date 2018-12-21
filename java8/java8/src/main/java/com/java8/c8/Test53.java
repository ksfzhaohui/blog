package com.java8.c8;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Test53 {

	public static void main(String[] args) {
		List<Dish> menu = Arrays.asList(new Dish("苹果", 100, Type.FRUIT), new Dish("猪肉", 400, Type.MEAT),
				new Dish("青菜", 500, Type.VEGETARIAN), new Dish("白菜", 350, Type.VEGETARIAN),
				new Dish("鸭肉", 450, Type.MEAT), new Dish("蛋糕", 150, Type.OTHER));

		// anyMatch 是否存在素菜
		System.out.println(menu.stream().anyMatch(Dish::isVegetarian));

		// allMatch 是否都匹配
		System.out.println(menu.stream().allMatch(d -> d.getCalories() < 1000));

		// noneMatch 没有匹配的
		System.out.println(menu.stream().noneMatch(d -> d.getCalories() >= 1000));

		// 获取一个任意元素
		Optional<Dish> dish = menu.stream().filter(Dish::isVegetarian).findAny();
		System.out.println(dish);

		// 查找第一个元素
		List<Integer> someNumbers = Arrays.asList(1, 2, 3, 4, 5);
		Optional<Integer> firstSquareDivisibleByThree = someNumbers.stream().map(x -> x * x).filter(x -> x % 3 == 0)
				.findFirst();
		System.out.println(firstSquareDivisibleByThree);
	}

}
