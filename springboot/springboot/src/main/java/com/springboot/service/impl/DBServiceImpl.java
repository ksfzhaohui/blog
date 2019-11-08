package com.springboot.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.dao.BlogDao;
import com.springboot.entity.Blog;
import com.springboot.service.DBService;

@Service
public class DBServiceImpl implements DBService {

	// Spring的 JdbcTemplate 和 NamedParameterJdbcTemplate 类会被自动配置
	private final JdbcTemplate jdbcTemplate;

	@Autowired
	private BlogDao blogDao;

	@Autowired
	public DBServiceImpl(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public String getBlog(long id) {
		Map map = jdbcTemplate.queryForMap("select * from blog where id=" + id);
		System.out.println(map.toString());
		return (String) map.get("content");
	}

	@Override
	public Blog getBlogJPA(long id) {
		Blog blog = blogDao.findById(id);
		System.err.println(blog.toString());
		return blog;
	}

}
