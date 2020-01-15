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
public class BillingInfo {
    @Id
    @Column(name = "AccountNumber", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "billing_info_generator")
    @SequenceGenerator(name="billing_info_generator", sequenceName = "billing_info_seq")
    private Long accountNumber;
    private String patientAddress;
    private String totalMedicalCosts; 
    @Transient
    private boolean inference;
} 