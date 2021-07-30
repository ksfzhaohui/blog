package com.zh.script.groovy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@SpringBootApplication 注解等价于以默认属性使用 @Configuration ， @EnableAutoConfiguration 和 @ComponentScan
//exclude禁用特定的自动配置项
//@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
// 通常建议将应用的main类放到其他类所在包的顶层(root package)，并将 @EnableAutoConfiguration
// 注解到你的main类上，这样就隐式地定义了一个基础的包搜索路径（search package）
@SpringBootApplication
public class SpringbootApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootApplication.class, args);
	}

}
