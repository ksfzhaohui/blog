package com.java8.c8;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.Comparator.comparing;

public class Test55 {

	public static void main(String[] args) {
		Trader raoul = new Trader("Raoul", "Cambridge");
		Trader mario = new Trader("Mario", "Milan");
		Trader alan = new Trader("Alan", "Cambridge");
		Trader brian = new Trader("Brian", "Cambridge");
		List<Transaction> transactions = Arrays.asList(new Transaction(brian, 2011, 300),
				new Transaction(raoul, 2012, 1000), new Transaction(raoul, 2011, 400),
				new Transaction(mario, 2012, 710), new Transaction(mario, 2012, 700), new Transaction(alan, 2012, 950));

		// 2011年交易排序
		List<Transaction> tr2011 = transactions.stream().filter(transaction -> transaction.getYear() == 2011)
				.sorted(comparing(Transaction::getValue)).collect(toList());
		System.out.println(tr2011);

		// 交易员工作城市
		List<String> cities = transactions.stream().map(transaction -> transaction.getTrader().getCity()).distinct()
				.collect(toList());
		System.out.println(cities);

		// 剑桥的，按名字排序
		List<Trader> traders = transactions.stream().map(Transaction::getTrader)
				.filter(trader -> trader.getCity().equals("Cambridge")).distinct().sorted(comparing(Trader::getName))
				.collect(toList());
		System.out.println(traders);

		// 有没有交易员在米兰工作
		boolean milanBased = transactions.stream()
				.anyMatch(transaction -> transaction.getTrader().getCity().equals("Milan"));
		System.out.println(milanBased);

		// 打印剑桥交易员交易额
		transactions.stream().filter(t -> "Cambridge".equals(t.getTrader().getCity())).map(Transaction::getValue)
				.forEach(System.out::println);

		// 最高交易额
		Optional<Integer> highestValue = transactions.stream().map(Transaction::getValue).reduce(Integer::max);
		System.out.println(highestValue);

		Optional<Transaction> smallestTransaction = transactions.stream()
				.reduce((t1, t2) -> t1.getValue() < t2.getValue() ? t1 : t2);
		System.out.println(smallestTransaction);
	}

}

class Trader {
	private final String name;
	private final String city;

	public Trader(String n, String c) {
		this.name = n;
		this.city = c;
	}

	public String getName() {
		return name;
	}

	public String getCity() {
		return city;
	}

}

class Transaction {
	private final Trader trader;
	private final int year;
	private final int value;

	public Transaction(Trader trader, int year, int value) {
		this.trader = trader;
		this.year = year;
		this.value = value;
	}

	public Trader getTrader() {
		return this.trader;
	}

	public int getYear() {
		return this.year;
	}

	public int getValue() {
		return this.value;
	}

	public String toString() {
		return "{" + this.trader + ", " + "year: " + this.year + ", " + "value:" + this.value + "}";
	}
}
