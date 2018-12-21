package com.java8.c2;

public class Apple {

	private String color;
	private Integer weight;

	public Apple() {

	}

	public Apple(Integer weight) {
		this.weight = weight;
	}

	public Apple(String color, Integer weight) {
		super();
		this.color = color;
		this.weight = weight;
	}

	public static boolean isGreenApple(Apple apple) {
		return "green".equals(apple.getColor());
	}

	public static boolean isHeavyApple(Apple apple) {
		return apple.getWeight() > 150;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return "Apple [color=" + color + ", weight=" + weight + "]";
	}

}
