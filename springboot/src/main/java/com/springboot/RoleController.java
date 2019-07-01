package com.springboot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.service.RoleService;

@RestController
public class RoleController {

	@Autowired
	private RoleService roleService;

	@RequestMapping("/role")
	public String getRole(long id) {
		return roleService.findRoleById(id).toString();
	}

}
