package com.example.demo.logic;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.example.demo.data.model.PatientMedicalInfo;
import com.example.demo.data.repository.PatientMedicalInfoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public class PatientMedicalInfoManager {

    
    private PatientMedicalInfoRepository patientMedicalnfoRepository;

    @Autowired
    private InferenceDetectionEngine inferenceDetectionEngine;
    
    @PersistenceContext
    private EntityManager entityManager;

    public PatientMedicalInfoManager(PatientMedicalInfoRepository patientMedicalnfoRepository) {
        this.patientMedicalnfoRepository = patientMedicalnfoRepository;
    }

    public List<PatientMedicalInfo> search(Long patientId, String lengthOfStay, String reasonOfVisit, Integer dailyMedicalCost) {
        List<PatientMedicalInfo> results = null;
        if(patientId == null && StringUtils.isEmpty(lengthOfStay) && StringUtils.isEmpty(reasonOfVisit) && dailyMedicalCost == null)
            results = patientMedicalnfoRepository.findAll();
        else 
            results = patientMedicalnfoRepository.customSearch(patientId, lengthOfStay, reasonOfVisit, dailyMedicalCost);
        results = inferenceDetectionEngine.checkInferenceForPatientMedicalInfo(results, Arrays.asList("patient_medical_info.id", "patient_medical_info.length_of_stay", "patient_medical_info.reason_of_visit", "patient_medical_info.daily_medical_cost"));
        return results;
    }

}
