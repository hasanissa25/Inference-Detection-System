/*package com.example.demo;

import com.example.demo.data.model.PatientInfo;
import com.example.demo.data.model.Policy;
import com.example.demo.data.repository.PatientMedicalInfoRepository;
import com.example.demo.data.repository.PatientlnfoRepository;
import com.example.demo.data.repository.PolicyRepository;
import com.example.demo.logic.InferenceDetectionEngine;
import com.example.demo.logic.PatientInfoManager;
import com.example.demo.logic.PatientMedicalInfoManager;

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest

public class InferencePreventionTest {
    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PatientMedicalInfoRepository patientMedicalInfoRepository;

    @Autowired
    private PatientlnfoRepository patientInfoRepository;

    @Autowired
    private PatientInfoManager pi;

    @Autowired
    private PatientMedicalInfoManager pmi;

    @Autowired
    private InferenceDetectionEngine inferenceDetectionEngine;


    private static final Logger log = LoggerFactory.getLogger(InferencePreventionTest.class);


    @Before
    public void before(){
        pi = new PatientInfoManager(patientInfoRepository);
        pmi = new PatientMedicalInfoManager(patientMedicalInfoRepository);
    }

    @Test
    public void shouldNotBlockNoInference() {
        //new policy
        Policy p = new Policy();
        p.setInputColumns(Arrays.asList("patient_medical_info.length_of_stay", "patient_info.date_of_entry",
                "patient_info.date_of_leave"));
        p.setBlockedColumns(Arrays.asList("patient_info.name"));
        p.setRelationship("patient_info.date_of_leave - patient_info.date_of_entry != patient_medical_info.length_of_stay");
        policyRepository.save(p);

        //search with policy
        pi.search("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M");
        pmi.search(null, "3", "Brain Aneurysm");
        List<PatientInfo> result = pi.search("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M");

        assertFalse(result.get(0).isInference());
        //Should not block any patient names
        assertFalse(result.get(0).getColumnValue("name").contains("Not Authorized"));
    }

    @Test
    public void shouldNotBlockInferenceForAdministrator() {
        Policy p = new Policy();
        p.setInputColumns(Arrays.asList("patient_medical_info.length_of_stay", "patient_info.date_of_entry",
                "patient_info.date_of_leave"));
        p.setBlockedColumns(Arrays.asList("patient_info.name"));
        p.setRelationship("patient_info.date_of_leave - patient_info.date_of_entry != patient_medical_info.length_of_stay");
        policyRepository.save(p);

        //search with policy
        pmi.search(null, "4", "Brain Aneurysm");
        List<PatientInfo> result = pi.search("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M");

        assertTrue(result.get(0).isInference());
        assertFalse(result.get(0).getColumnValue("name").contains("Not Authorized"));
    }
}*/