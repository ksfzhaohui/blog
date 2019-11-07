package com.mybatis.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Flush;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import com.mybatis.vo.Blog;
import com.mybatis.vo.BlogEnum;
import com.mybatis.vo.BlogNew;

public interface BlogMapper<T> extends ParentMapper {

	public default String defaultValue() {
		return "default value";
	}

	public Blog selectBlog(@Param("id") long id);

	@Select("SELECT * FROM blog WHERE id = #{id}")
	public Blog selectBlog2(long id);

	// @Flush
	public Blog selectBlog3(@Param("id")long id,@Param("author") String author);
	public Blog selectBlogMap(@Param("id")long id,@Param("author") String author);
	public BlogEnum selectBlogMap2(@Param("id")long id,@Param("author") String author);

	public T selectBlog4(long id);

	public void updateBlog(Blog blog);

	public List<Blog> selectBlogs(String author);

//	@MapKey("xx")
	public List<Blog> selectBlogsArray(String author);

	public void selectBlogsByHandler(String author, ResultHandler handler);

	public List<Blog> selectBlogsByRowBounds(String author, RowBounds rowBounds);

	public void insertBlog(Blog blog);
}
