package com.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.format.FormatService;

@RestController // 被称为构造型（stereotype） 注解,它为阅读代码的人提供暗示（这是一个支持REST的控制器）
public class FormatController {

	@Autowired
	private FormatService formatService;


	/**
	 * http://localhost:8888/format?word=hello
	 * 
	 * @param word
	 * @return
	 */
	@RequestMapping("/format")
	public String input(String word) {
		return formatService.wrap(word);
	}

}
