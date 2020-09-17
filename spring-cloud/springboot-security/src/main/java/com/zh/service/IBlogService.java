package com.zh.service;

import java.util.List;

import com.zh.entity.Blog;

/**
 * Created by fangzhipeng on 2017/5/15.
 */
public interface IBlogService {
    List<Blog> getBlogs();
    void deleteBlog(long id);
}
