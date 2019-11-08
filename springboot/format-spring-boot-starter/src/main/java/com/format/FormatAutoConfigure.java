package com.format;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(FormatService.class)
@EnableConfigurationProperties(FormatServiceProperties.class)
public class FormatAutoConfigure {

	@Autowired
	private FormatServiceProperties properties;

	@Bean
	@ConditionalOnMissingBean
	FormatService formatService() {
		return new FormatService(properties.getType());
	}

}