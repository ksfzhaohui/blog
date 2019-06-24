package com.springboot;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.springboot.listener.MyListener;

/**
 * 如果默认的 SpringApplication 不符合你的口味，你可以创建一个本地实例并对它进行自定义
 * 
 * @author hui.zhao.cfs
 *
 */
@SpringBootApplication
public class CustomSpringbootApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(CustomSpringbootApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		app.addListeners(new MyListener());
//		app.setWebApplicationType(WebApplicationType.NONE);
		app.run(args);
	}

}
