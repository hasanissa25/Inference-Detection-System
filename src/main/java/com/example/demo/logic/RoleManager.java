package com.example.demo.logic;

import com.example.demo.data.repository.RoleRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import com.example.demo.data.model.Role;

@Component
public class RoleManager {
    private RoleRepository roleRepository;

    public RoleManager(RoleRepository roleRepository){
        this.roleRepository = roleRepository;
    }

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}