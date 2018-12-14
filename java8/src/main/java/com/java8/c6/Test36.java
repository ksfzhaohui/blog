package com.java8.c6;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Comparator.comparing;

import java.util.ArrayList;

import com.java8.c2.Apple;

public class Test36 {

	public static void main(String[] args) {
		List<Apple> inventory = Arrays.asList(new Apple("green", 200), new Apple("red", 100), new Apple("black", 110),
				new Apple("blue", 300));
		inventory.sort((Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight()));

		// 方法引用
		inventory.sort(comparing(Apple::getWeight));

		List<String> str = Arrays.asList("a", "b", "A", "B");
		str.sort((s1, s2) -> s1.compareToIgnoreCase(s2));
		System.out.println(str);

		List<String> str2 = Arrays.asList("a", "b", "A", "B");
		str2.sort(String::compareToIgnoreCase);
		System.out.println(str2);

		// 构造函数引用
		Supplier<Apple> c1 = Apple::new;
		Apple a1 = c1.get();
		System.out.println(a1);

		Function<Integer, Apple> c2 = Apple::new;
		Apple a2 = c2.apply(110);
		System.out.println(a2);

		BiFunction<String, Integer, Apple> c3 = Apple::new;
		Apple a3 = c3.apply("green", 110);
		System.out.println(a3);

		List<Integer> weights = Arrays.asList(7, 3, 4, 10);
		List<Apple> apples = map(weights, Apple::new);
		System.out.println(apples);
	}

	public static List<Apple> map(List<Integer> list, Function<Integer, Apple> f) {
		List<Apple> result = new ArrayList<>();
		for (Integer e : list) {
			result.add(f.apply(e));
		}
		return result;
	}

}
