package com.java8.c8;

import java.util.Arrays;
import java.util.List;
import static java.util.stream.Collectors.toList;

/**
 * 筛选和切片
 * 
 * @author hui.zhao.cfs
 *
 */
public class Test51 {

	public static void main(String[] args) {
		// 谓词筛选
		List<Dish> menu = Arrays.asList(new Dish("苹果", 100, Type.FRUIT), new Dish("猪肉", 400, Type.MEAT),
				new Dish("青菜", 500, Type.VEGETARIAN), new Dish("白菜", 350, Type.VEGETARIAN),
				new Dish("鸭肉", 450, Type.MEAT), new Dish("蛋糕", 150, Type.OTHER));
		List<Dish> vegetarianMenu = menu.stream().filter(Dish::isVegetarian).collect(toList());
		System.out.println(vegetarianMenu);

		// distinct去重
		List<Integer> numbers = Arrays.asList(1, 2, 1, 3, 3, 2, 4);
		numbers.stream().filter(i -> i % 2 == 0).distinct().forEach(System.out::println);

		// limit指定数量
		List<Dish> dishes = menu.stream().filter(d -> d.getCalories() > 300).limit(3).collect(toList());
		System.out.println(dishes);

		// skip跳过元素
		List<Dish> dishes2 = menu.stream().filter(d -> d.getCalories() > 300).skip(2).collect(toList());
		System.out.println(dishes2);
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
