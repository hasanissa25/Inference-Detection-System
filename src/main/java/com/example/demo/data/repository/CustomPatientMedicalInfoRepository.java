package com.example.demo.data.repository;

import java.util.List;

import com.example.demo.data.model.PatientMedicalInfo;

public interface CustomPatientMedicalInfoRepository {

    List<PatientMedicalInfo> customSearch(Long patientId, String lengthOfStay, String reasonOfVisit, Integer dailyMedicalCost);
    
}
