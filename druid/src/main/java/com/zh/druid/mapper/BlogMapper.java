package com.zh.druid.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.session.RowBounds;

import com.zh.druid.vo.Blog;

@Mapper
public interface BlogMapper {

	public List<Blog> selectBlogs(String author);

	public List<Blog> selectBlogs(String author, RowBounds rowBounds);
	
	public int countBlogs();

}
