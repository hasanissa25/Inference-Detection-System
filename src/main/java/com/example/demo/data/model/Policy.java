package com.example.demo.data.model;

import java.util.List;
import java.util.Map;

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
public class Policy {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "policy_generator")
    @SequenceGenerator(name="policy_generator", sequenceName = "policy_seq")
    private Long id;
    @ElementCollection
    private List<String> inputColumns;
    @ElementCollection 
    private List<String> blockedColumns;
    private String relationship;

    public boolean processCriteria(DBLogEntry entry) {
        //TODO: placeholder to make policy criteria generic
        return true;
	}
}
