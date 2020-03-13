package com.example.demo.data.repository;

import java.util.List;

import com.example.demo.data.model.PatientInfo;
import com.example.demo.data.model.PatientMedicalInfo;
import org.springframework.data.jpa.repository.Query;

public interface CustomPatientMedicalInfoRepository {
    @Query("from PatientMedicalInfo")
    List<PatientMedicalInfo> customSearch(Long patientId, String lengthOfStay, String reasonOfVisit);
    @Query("from PatientMedicalInfo")
    PatientMedicalInfo findById(Long patientId);
}
