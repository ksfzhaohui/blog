package com.zh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**1.application.yml中指定环境
 * 2.java -jar xx.jar --spring.profiles.active=dev
 * 
 * @author hui.zhao
 *
 */
@SpringBootApplication
public class SpringbootFirstApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringbootFirstApplication.class, args);
	}
}
