package com.example.demo.data.repository;

import com.example.demo.data.model.Role;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer>{

    public Role findByName(String name);
}