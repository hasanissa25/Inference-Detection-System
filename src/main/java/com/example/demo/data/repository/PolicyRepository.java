package com.example.demo.data.repository;

import java.util.List;

import com.example.demo.data.model.Policy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    List<Policy> findDistinctByInputColumnsInAndBlockedColumnsIn(List<String> inputColumnsList, List<String> blockedColumnsList);
}
