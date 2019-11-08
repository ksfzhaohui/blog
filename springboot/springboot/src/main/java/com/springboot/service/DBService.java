package com.springboot.service;

import com.springboot.entity.Blog;

public interface DBService {
	
	public String getBlog(long id);

	Blog getBlogJPA(long id);

}
