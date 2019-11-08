package com.format;

public class FormatService {

	private String type;

	public FormatService(String type) {
		this.type = type;
	}

	public String wrap(String word) {
		if(type.equalsIgnoreCase("Upper")){//大写
			return word.toUpperCase();
		}else if(type.equalsIgnoreCase("Lower")){//小写
			return word.toLowerCase();
		}
		return word;
	}
}
