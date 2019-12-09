package com.zh.druid.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JdbcController {

	@Autowired
	JdbcTemplate jdbcTemplate;

	@ResponseBody
	@GetMapping("/query")
	public Map<String, Object> map() {
		List<Map<String, Object>> list = jdbcTemplate.queryForList("select * FROM blog");
		return list.get(0);
	}
}
