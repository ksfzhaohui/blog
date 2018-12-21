package com.java8.c9;

import static java.util.stream.Collectors.averagingInt;
import static java.util.stream.Collectors.maxBy;
import static java.util.stream.Collectors.summarizingInt;
import static java.util.stream.Collectors.summingInt;

import java.util.Arrays;
import java.util.Comparator;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Test62 {

	public static void main(String[] args) {
		List<Dish> menu = Arrays.asList(new Dish("苹果", 100, Type.FRUIT), new Dish("猪肉", 400, Type.MEAT),
				new Dish("青菜", 500, Type.VEGETARIAN), new Dish("白菜", 350, Type.VEGETARIAN),
				new Dish("鸭肉", 450, Type.MEAT), new Dish("蛋糕", 150, Type.OTHER));

		long howManyDishes = menu.stream().collect(Collectors.counting());
		System.out.println(howManyDishes);

		// 最大值最小值
		Comparator<Dish> dishCaloriesComparator = Comparator.comparingInt(Dish::getCalories);
		Optional<Dish> mostCalorieDish = menu.stream().collect(maxBy(dishCaloriesComparator));
		System.out.println(mostCalorieDish);

		// 汇总
		int totalCalories = menu.stream().collect(summingInt(Dish::getCalories));
		System.out.println(totalCalories);

		// 平均值
		double avgCalories = menu.stream().collect(averagingInt(Dish::getCalories));
		System.out.println(avgCalories);

		IntSummaryStatistics menuStatistics = menu.stream().collect(summarizingInt(Dish::getCalories));
		System.out.println(menuStatistics);

		// 连接字符串
		String shortMenu = menu.stream().map(Dish::getName).collect(Collectors.joining(","));
		System.out.println(shortMenu);

		// ======广义的归约：Collectors.reducing是所有工厂方法的一般化=======
		totalCalories = menu.stream().collect(Collectors.reducing(0, Dish::getCalories, (i, j) -> i + j));
		System.out.println(totalCalories);

		mostCalorieDish = menu.stream()
				.collect(Collectors.reducing((d1, d2) -> d1.getCalories() > d2.getCalories() ? d1 : d2));
		System.out.println(mostCalorieDish);
	}

}

enum Type {
	MEAT, FRUIT, VEGETARIAN, OTHER;
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

	public boolean isVegetarian() {
		if (type == Type.VEGETARIAN) {
			return true;
		}
		return false;
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

	@Override
	public String toString() {
		return "Dish [calories=" + calories + ", name=" + name + ", type=" + type + "]";
	}

}
