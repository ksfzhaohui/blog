package com.java8.c6;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import com.java8.c2.Apple;
import static java.util.Comparator.comparing;

public class Test38 {

	public static void main(String[] args) {
		List<Apple> inventory = Arrays.asList(new Apple("green", 200), new Apple("red", 100), new Apple("black", 110),
				new Apple("blue", 300));

		// 1.比较器复合
		// 逆序
		inventory.sort(comparing(Apple::getWeight).reversed());

		// 比较器链
		inventory.sort(comparing(Apple::getWeight).reversed().thenComparing(Apple::getColor));

		// 2.谓词复合
		Predicate<Apple> redApple = (Apple a) -> "red".equals(a.getColor());
		Predicate<Apple> notRedApple = redApple.negate();
		Predicate<Apple> redAndHeavyAppleOrGreen = redApple.and(a -> a.getWeight() > 150)
				.or(a -> "green".equals(a.getColor()));

		// 3.函数复合
		Function<Integer, Integer> f = x -> x + 1;
		Function<Integer, Integer> g = x -> x * 2;
		Function<Integer, Integer> h = f.andThen(g);
		int result = h.apply(1);
		System.out.println(result);
	}

}
