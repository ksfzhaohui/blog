package com.java8.c3;

import java.util.ArrayList;
import static java.util.stream.Collectors.toList;
import java.util.List;

import com.java8.c2.Apple;

public class Test1 {

	public static void main(String[] args) {
		List<Apple> inventory = new ArrayList<>();
		inventory.add(new Apple("green", 200));
		inventory.add(new Apple("red", 100));
		inventory.add(new Apple("black", 110));
		inventory.add(new Apple("blue", 300));

		System.out.println(inventory.stream().filter((Apple a) -> a.getWeight() > 150).collect(toList()));
		System.out.println(inventory.parallelStream().filter((Apple a) -> a.getWeight() > 150).collect(toList()));
	}

}
