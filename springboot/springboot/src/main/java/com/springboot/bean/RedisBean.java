package com.springboot.bean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisBean {

	private StringRedisTemplate template;

	@Autowired
	public RedisBean(StringRedisTemplate template) {
		this.template = template;
	}

	public StringRedisTemplate getTemplate() {
		return template;
	}
	
	public void set(String key,String value){
		template.opsForValue().set(key, value);
	}

	public String get(String key){
		return template.opsForValue().get(key);
	}
}
