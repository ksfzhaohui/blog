package com.java8.c7;

import java.util.Arrays;
import java.util.List;
import static java.util.stream.Collectors.toList;

public class Test44 {

	public static void main(String[] args) {
		List<Dish> menu = Arrays.asList(new Dish("苹果", 100, Type.FRUIT), new Dish("猪肉", 400, Type.MEAT),
				new Dish("牛肉", 500, Type.MEAT), new Dish("鸡肉", 350, Type.MEAT), new Dish("鸭肉", 450, Type.MEAT),
				new Dish("蛋糕", 150, Type.OTHER));
		List<String> names = menu.stream().filter(d -> {
			System.out.println("filtering" + d.getName());
			return d.getCalories() > 300;
		}).map(d -> {
			System.out.println("mapping" + d.getName());
			return d.getName();
		}).limit(3).collect(toList());
		System.out.println(names);
	}

}
