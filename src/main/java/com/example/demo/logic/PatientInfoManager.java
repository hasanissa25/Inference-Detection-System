package com.example.demo.logic;

import java.util.Arrays;
import java.util.List;
import java.time.Duration;
import java.time.Instant;
import com.example.demo.data.model.PatientInfo;
import com.example.demo.data.repository.PatientlnfoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public class PatientInfoManager {
    
    private PatientlnfoRepository patientInfoRepository;
    
    @Autowired
    private InferenceDetectionEngine inferenceDetectionEngine;
    
    public PatientInfoManager(PatientlnfoRepository patientInfoRepository) {
        this.patientInfoRepository = patientInfoRepository;
    }
    
    public List<PatientInfo> search(String patientName, String dateOfEntry, String dateOfLeave, String gender) {
        
        Instant start = Instant.now();
        
       
        
        List<PatientInfo> results = null;
        
        if(StringUtils.isEmpty(patientName) && StringUtils.isEmpty(dateOfEntry) && StringUtils.isEmpty(dateOfLeave) && StringUtils.isEmpty(gender))
        results = patientInfoRepository.findAll();
        else 
        results = patientInfoRepository.findByNameIgnoreCaseOrDateOfEntryOrDateOfLeaveOrGenderIgnoreCase(patientName, dateOfEntry, dateOfLeave, gender);
        
        
        results = inferenceDetectionEngine.checkInferenceForPatientInfo(results, Arrays.asList("patient_info.name", "patient_info.date_of_entry", "patient_info.date_of_leave", "patient_info.gender"));
        //your code
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        System.out.println("TIME TAKEN: "+ timeElapsed.toMillis() +" MS------------");
        return results;
    }
}
