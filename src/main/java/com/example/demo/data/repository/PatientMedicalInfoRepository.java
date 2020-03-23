package com.example.demo.data.repository;

import com.example.demo.data.model.PatientMedicalInfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientMedicalInfoRepository extends JpaRepository<PatientMedicalInfo, Long>, CustomPatientMedicalInfoRepository {
    
}
