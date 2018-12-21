package com.java8.c8;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * 从元素中选择指定的信息，类似Sql中的选择某一列
 * 
 * @author hui.zhao.cfs
 *
 */
public class Test52 {

	public static void main(String[] args) {

		// 对流中的每个元素应用函数
		List<Dish> menu = Arrays.asList(new Dish("苹果", 100, Type.FRUIT), new Dish("猪肉", 400, Type.MEAT),
				new Dish("青菜", 500, Type.VEGETARIAN), new Dish("白菜", 350, Type.VEGETARIAN),
				new Dish("鸭肉", 450, Type.MEAT), new Dish("蛋糕", 150, Type.OTHER));
		List<String> dishNames = menu.stream().map(Dish::getName).collect(toList());
		System.out.println(dishNames);

		List<String> words = Arrays.asList("Java 8", "Lambdas", "In", "Action");
		List<Integer> wordLengths = words.stream().map(String::length).collect(toList());
		System.out.println(wordLengths);

		List<Integer> dishNameLengths = menu.stream().map(Dish::getName).map(String::length).collect(toList());
		System.out.println(dishNameLengths);

		// 流的扁平化
		List<String> words2 = Arrays.asList("Java8", "Lambdas", "In", "Action");
		List<String[]> wArrays = words2.stream().map(word -> word.split("")).distinct().collect(toList());
		System.out.println(wArrays);

		System.out.println(words.stream().map(word -> word.split("")).map(Arrays::stream).distinct().collect(toList()));

		// flatmap的效果，各个数组并不是分别映射成流，而是映射成流的内容
		List<String> uniqueCharacters = words.stream().map(w -> w.split("")).flatMap(Arrays::stream).distinct()
				.collect(Collectors.toList());
		System.out.println(uniqueCharacters);
	}

}
