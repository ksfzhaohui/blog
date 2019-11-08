package com.format;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("format.service")
public class FormatServiceProperties {

	private String type;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}