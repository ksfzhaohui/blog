package com.zh.druid.controller;

import java.util.List;

import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.zh.druid.repository.BlogRepository;
import com.zh.druid.vo.Blog;

@RestController
public class BlogController {

	@Autowired
	private BlogRepository blogRepository;

	@ResponseBody
	@GetMapping("/blog")
	public String rowBounds() {
		int pageSize = 10;
		int totalCount = blogRepository.countBlogs();
		int totalPages = (totalCount % pageSize == 0) ? totalCount / pageSize : totalCount / pageSize + 1;
		System.out.println("[pageSize=" + pageSize + ",totalCount=" + totalCount + ",totalPages=" + totalPages + "]");
		for (int currentPage = 0; currentPage < totalPages; currentPage++) {
			List<Blog> blogs = blogRepository.selectBlogs("zhaohui", new RowBounds(currentPage * pageSize, pageSize));
			System.err.println("currentPage=" + (currentPage + 1) + ",current size:" + blogs.size());
			for(Blog blog:blogs){
				System.out.println(blog);
			}
		}
		return "ok";
	}
}
