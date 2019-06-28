package com.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ExampleService;
import com.springboot.bean.RedisBean;
import com.springboot.service.DBService;
import com.springboot.service.HelloService;

@RestController // 被称为构造型（stereotype） 注解,它为阅读代码的人提供暗示（这是一个支持REST的控制器）
@EnableAutoConfiguration // 告诉Spring Boot根据添加的jar依赖猜测你想如何配置Spring
public class HelloController {
	@Autowired
	private HelloService helloService;

	@Autowired
	private DBService dbService;

	@Autowired
	private RedisBean redisBean;

	@Autowired
	private ExampleService exampleService;

	// 注解提供路由信息，它告诉Spring任何来自"/"路径的HTTP请求都应该被映射到 home 方法
	@RequestMapping("/")
	String home() {
		return helloService.hello();
	}

	@RequestMapping("/db")
	String db() {
		return dbService.getBlog(101);
	}

	@RequestMapping("/dbjpa")
	String dbJPA() {
		return dbService.getBlogJPA(101).toString();
	}

	@RequestMapping("/redis")
	String redisSet() {
		redisBean.set("hello", "nbl");
		return redisBean.get("hello");
	}

	/**
	 * http://localhost:8888/example?word=hello
	 * 
	 * @param word
	 * @return
	 */
	@RequestMapping("/example")
	public String input(String word) {
		return exampleService.wrap(word);
	}

}
