package com.example.demo.data.repository;

import com.example.demo.data.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>{

    public User findByUserName(String userName);
}