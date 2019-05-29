package com.mybatis.mapper;

import org.apache.ibatis.annotations.Select;

import com.mybatis.vo.Blog;

public interface BlogMapper {

	public Blog selectBlog(long id);

	@Select("SELECT * FROM blog WHERE id = #{id}")
	public Blog selectBlog2(long id);

}
