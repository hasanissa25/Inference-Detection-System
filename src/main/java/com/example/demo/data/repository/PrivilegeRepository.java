package com.example.demo.data.repository;

import com.example.demo.data.model.Privilege;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrivilegeRepository extends JpaRepository<Privilege, Integer>{

    public Privilege findByName(String name);
}