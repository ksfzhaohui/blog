package com.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Select;

import com.mybatis.vo.Blog;

public interface BlogMapper {

	public Blog selectBlog(long id);

	@Select("SELECT * FROM blog WHERE id = #{id}")
	public Blog selectBlog2(long id);

	public void updateBlog(Blog blog);

	public List<Blog> selectBlogs(String author);
}
