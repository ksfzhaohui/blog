package com.springboot.bean;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 如果需要在 SpringApplication 启动后执行一些特殊的代码，你可以实现 ApplicationRunner 或
 * CommandLineRunner 接口;这两个接口工作方式相 同，都只提供单一的 run 方法，该方法仅在
 * SpringApplication.run(…) 完成 之前调用
 * 
 * @author hui.zhao.cfs
 *
 */
@Component
public class MyCommandLineRunner implements CommandLineRunner {

	@Override
	public void run(String... args) throws Exception {
		System.err.println("MyCommandLineRunner:" + args);
	}

}
