package com.example.demo.data.repository;

import java.util.List;

import com.example.demo.data.model.DBLogEntry;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DBLogEntryRepository extends JpaRepository<DBLogEntry, Long> {
    List<DBLogEntry> findDistinctByTablesColumnsAccessedIn(List<String> tablesAndColumnsAccessed);
}
