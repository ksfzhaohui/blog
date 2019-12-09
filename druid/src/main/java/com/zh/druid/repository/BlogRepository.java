package com.zh.druid.repository;

import java.util.List;

import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.zh.druid.mapper.BlogMapper;
import com.zh.druid.vo.Blog;

@Repository
public class BlogRepository {

    @Autowired
    private BlogMapper blogMapper;
    
    public List<Blog> selectBlogs(String author,RowBounds rowBounds){
    	return blogMapper.selectBlogs(author,rowBounds);
    }
    
    public List<Blog> selectBlogs(String author){
    	return blogMapper.selectBlogs(author);
    }
    
    public int countBlogs(){
    	return blogMapper.countBlogs();
    }

}
