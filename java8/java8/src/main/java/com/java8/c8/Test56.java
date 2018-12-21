package com.java8.c8;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Test56 {

	public static void main(String[] args) {
		List<Dish> menu = Arrays.asList(new Dish("苹果", 100, Type.FRUIT), new Dish("猪肉", 400, Type.MEAT),
				new Dish("青菜", 500, Type.VEGETARIAN), new Dish("白菜", 350, Type.VEGETARIAN),
				new Dish("鸭肉", 450, Type.MEAT), new Dish("蛋糕", 150, Type.OTHER));
		// 将流转换为特化版本常用方法mapToInt,mapToDouble,mapToLong
		int calories = menu.stream().mapToInt(Dish::getCalories).sum();
		System.out.println(calories);

		// 转回为对象流
		IntStream intStream = menu.stream().mapToInt(Dish::getCalories);
		Stream<Integer> stream = intStream.boxed();
		System.out.println(stream);

		OptionalInt maxCalories = menu.stream().mapToInt(Dish::getCalories).max();
		System.out.println(maxCalories);

		// 数值范围 -- 1-100的偶数
		IntStream evenNumbers = IntStream.rangeClosed(1, 100).filter(n -> n % 2 == 0);
		System.out.println(evenNumbers.count());

		Stream<int[]> pythagoreanTriples = IntStream.rangeClosed(1, 100).boxed()
				.flatMap(a -> IntStream.rangeClosed(a, 100).filter(b -> Math.sqrt(a * a + b * b) % 1 == 0)
						.mapToObj(b -> new int[] { a, b, (int) Math.sqrt(a * a + b * b) }));
		pythagoreanTriples.limit(5).forEach(t -> System.out.println(t[0] + ", " + t[1] + ", " + t[2]));
	}

}
