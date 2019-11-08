package com.springboot;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import com.springboot.listener.MyListener;

/**
 * 如果默认的 SpringApplication 不符合你的口味，你可以创建一个本地实例并对它进行自定义
 * 
 * @author hui.zhao.cfs
 *
 */
@SpringBootApplication
@EnableCaching
public class CustomSpringbootApplication {

	public static void main(String[] args) {
		SpringApplication app = new SpringApplication(CustomSpringbootApplication.class);
		app.setBannerMode(Banner.Mode.LOG);
		app.addListeners(new MyListener());
//		app.setWebApplicationType(WebApplicationType.NONE);
		
		//以编程方式设置profiles
		app.setAdditionalProfiles("prod");
		app.run(args);
	}

}
