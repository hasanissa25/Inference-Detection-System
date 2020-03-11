package com.example.demo.data.repository;

import com.example.demo.data.model.PatientMedicalInfo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientMedicalInfoRepository extends JpaRepository<PatientMedicalInfo, String>, CustomPatientMedicalInfoRepository {

}
