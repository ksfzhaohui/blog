package com.springboot.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springboot.dao.RoleDao;
import com.springboot.entity.Role;
import com.springboot.service.RoleService;

@Service
public class RoleServiceImpl implements RoleService {

	@Autowired
	private RoleDao roleDao;

	@Override
	public Role findRoleById(long roleId) {
		return roleDao.findRoleById(roleId);
	}

}
