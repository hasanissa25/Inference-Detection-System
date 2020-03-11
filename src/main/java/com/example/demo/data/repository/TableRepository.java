package com.example.demo.data.repository;

import java.util.List;

import com.example.demo.data.model.Policy;

import com.example.demo.data.model.SuperTable;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TableRepository extends JpaRepository<SuperTable, Long> {


}
