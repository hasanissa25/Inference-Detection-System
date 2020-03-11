package com.example.demo.data.repository;

import java.util.List;

import com.example.demo.data.model.SuperTable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TableRepository extends JpaRepository<SuperTable, Long> {

    @Query(value = "select * from SUPER_TABLE where patient_id = :id OR select * from SUPER_TABLE where name = :id", nativeQuery = true)
    List<SuperTable> findByIdsAccessed(@Param("id") String id);
}
