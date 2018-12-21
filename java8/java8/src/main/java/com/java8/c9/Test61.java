package com.java8.c9;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;

public class Test61 {

	public static void main(String[] args) {

		Trader raoul = new Trader("Raoul", "Cambridge");
		Trader mario = new Trader("Mario", "Milan");
		Trader alan = new Trader("Alan", "Cambridge");
		Trader brian = new Trader("Brian", "Cambridge");

		Currency CNY = new Currency("CNY");
		Currency USD = new Currency("USD");

		List<Transaction> transactions = Arrays.asList(new Transaction(brian, 2011, 300, CNY),
				new Transaction(raoul, 2012, 1000, CNY), new Transaction(raoul, 2011, 400, CNY),
				new Transaction(mario, 2012, 710, USD), new Transaction(mario, 2012, 700, USD),
				new Transaction(alan, 2012, 950, USD));

		// Collectors提供了很多默认的收集器，当然也可以自定义收集器
		Map<Currency, List<Transaction>> transactionsByCurrencies = transactions.stream()
				.collect(groupingBy(Transaction::getCurrency));
		System.out.println(transactionsByCurrencies.keySet());
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

class Currency {
	private String currency;

	public Currency(String currency) {
		this.currency = currency;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

}

class Transaction {
	private final Trader trader;
	private final Currency currency;
	private final int year;
	private final int value;

	public Transaction(Trader trader, int year, int value, Currency currency) {
		this.trader = trader;
		this.year = year;
		this.value = value;
		this.currency = currency;
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

	public Currency getCurrency() {
		return currency;
	}

	@Override
	public String toString() {
		return "Transaction [trader=" + trader + ", currency=" + currency + ", year=" + year + ", value=" + value + "]";
	}

}
