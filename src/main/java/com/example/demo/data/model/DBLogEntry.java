package com.example.demo.data.model;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class DBLogEntry {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "db_log_entry_generator")
    @SequenceGenerator(name="db_log_entry_generator", sequenceName = "db_log_entry_seq")
    private Long id;
    private String userName;
    @ElementCollection
    private List<String> tablesColumnsAccessed;
    @ElementCollection
    private List<String> idsAccessed;
    private LocalDateTime dateAccessed;
}