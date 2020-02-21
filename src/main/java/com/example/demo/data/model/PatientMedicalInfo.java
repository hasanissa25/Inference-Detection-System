package com.example.demo.data.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientMedicalInfo {
    @Id
    @Column(name = "patientId", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "patient_medical_info_generator")
    @SequenceGenerator(name="patient_medical_info_generator", sequenceName = "patient_medical_info_seq")
    private Long patientId;
    private String lengthOfStay;
    private String reasonOfVisit; 
    private Integer dailyMedicalCost; 
    @Transient
    private boolean inference;
} 