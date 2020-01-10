package com.example.demo.data.repository;

import com.example.demo.data.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer>{
}