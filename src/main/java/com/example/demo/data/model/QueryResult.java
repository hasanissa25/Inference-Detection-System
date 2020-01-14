package com.example.demo.data.model;

import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapKeyColumn;
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
public class QueryResult {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "db_query_result_generator")
    @SequenceGenerator(name="db_query_result_generator", sequenceName = "db_query_result_seq")
    private Long id;

    @ElementCollection
    @CollectionTable(name = "query_result_rows_mapping", 
      joinColumns = {@JoinColumn(name = "query_result_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "table_column_name")
    @Column(name = "table_column_value")
    private Map<String, String> queryResultRowMap;
}