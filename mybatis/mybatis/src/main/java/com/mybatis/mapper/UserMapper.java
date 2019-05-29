package com.mybatis.mapper;

import com.mybatis.vo.User;

public interface UserMapper {

	public User getUser(long id);

	public void insertUser(User user);

}
