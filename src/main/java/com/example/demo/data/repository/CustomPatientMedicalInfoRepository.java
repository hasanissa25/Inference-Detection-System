package com.example.demo.data.repository;

import java.util.List;

import com.example.demo.data.model.PatientInfo;
import com.example.demo.data.model.PatientMedicalInfo;
import org.springframework.data.jpa.repository.Query;

public interface CustomPatientMedicalInfoRepository {
    @Query("from PatientMedicalInfo")
    List<PatientMedicalInfo> customSearch(String patientId, String lengthOfStay, String reasonOfVisit);

    PatientMedicalInfo findByPatientID(String patientId);
}
