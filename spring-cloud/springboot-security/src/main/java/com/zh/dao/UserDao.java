package com.zh.dao;


import org.springframework.data.jpa.repository.JpaRepository;

import com.zh.entity.User;


public interface UserDao extends JpaRepository<User, Long>{

	User findByUsername(String username);
}
