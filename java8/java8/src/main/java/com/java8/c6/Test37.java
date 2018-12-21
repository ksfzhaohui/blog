package com.java8.c6;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.java8.c2.Apple;
import static java.util.Comparator.comparing;

public class Test37 {

	public static void main(String[] args) {
		// 1.传递代码
		List<Apple> inventory = Arrays.asList(new Apple("green", 200), new Apple("red", 100), new Apple("black", 110),
				new Apple("blue", 300));
		inventory.sort(new AppleComparator());

		// 2.使用匿名类
		inventory.sort(new Comparator<Apple>() {
			public int compare(Apple a1, Apple a2) {
				return a1.getWeight().compareTo(a2.getWeight());
			}
		});

		// 3.使用lambda表达式
		inventory.sort((Apple a1, Apple a2) -> a1.getWeight().compareTo(a2.getWeight()));

		// 4.使用方法引用
		inventory.sort(comparing(Apple::getWeight));

	}

}

class AppleComparator implements Comparator<Apple> {
	public int compare(Apple a1, Apple a2) {
		return a1.getWeight().compareTo(a2.getWeight());
	}
}
