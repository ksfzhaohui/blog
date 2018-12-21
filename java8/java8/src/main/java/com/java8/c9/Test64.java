package com.java8.c9;

import static java.util.stream.Collectors.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static java.util.Comparator.comparingInt;

/**
 * 分区是分组的特殊形式：由一个谓词（返回一个boolean的函数）作为分类函数，称为分区函数
 * 
 * @author hui.zhao.cfs
 *
 */
public class Test64 {

	public static void main(String[] args) {
		List<Dish> menu = Arrays.asList(new Dish("苹果", 100, Type.FRUIT), new Dish("猪肉", 400, Type.MEAT),
				new Dish("青菜", 500, Type.VEGETARIAN), new Dish("白菜", 350, Type.VEGETARIAN),
				new Dish("鸭肉", 450, Type.MEAT), new Dish("蛋糕", 150, Type.OTHER));
		Map<Boolean, List<Dish>> partitionedMenu = menu.stream().collect(partitioningBy(Dish::isVegetarian));
		System.out.println(partitionedMenu);

		Map<Boolean, Map<Type, List<Dish>>> vegetarianDishesByType = menu.stream()
				.collect(partitioningBy(Dish::isVegetarian, groupingBy(Dish::getType)));
		System.out.println(vegetarianDishesByType);

		Map<Boolean, Dish> mostCaloricPartitionedByVegetarian = menu.stream().collect(partitioningBy(Dish::isVegetarian,
				collectingAndThen(maxBy(comparingInt(Dish::getCalories)), Optional::get)));
		System.out.println(mostCaloricPartitionedByVegetarian);
	}

}
