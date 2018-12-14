package com.java8.c7;

import java.util.ArrayList;
import static java.util.stream.Collectors.toList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class Test43 {

	public static void main(String[] args) {
		List<String> title = Arrays.asList("Java8", "In", "Action");
		Stream<String> s = title.stream();
		s.forEach(System.out::println);
		// 流只能消费一次
		//s.forEach(System.out::println);

		// 外部迭代
		List<Dish> menu = Arrays.asList(new Dish("苹果", 100, Type.FRUIT), new Dish("猪肉", 200, Type.MEAT),
				new Dish("蛋糕", 150, Type.OTHER));
		List<String> names = new ArrayList<>();
		for (Dish d : menu) {
			names.add(d.getName());
		}
		System.out.println(names);

		// 内部迭代
		List<String> names2 = menu.stream().map(Dish::getName).collect(toList());
		System.out.println(names2);
	}

}
