/*

package com.example.demo;

import com.example.demo.config.LoadData;
import com.example.demo.data.model.PatientInfo;
import com.example.demo.data.model.PatientMedicalInfo;
import com.example.demo.data.model.Policy;
import com.example.demo.data.repository.PatientMedicalInfoRepository;
import com.example.demo.data.repository.PatientlnfoRepository;
import com.example.demo.data.repository.PolicyRepository;
import com.example.demo.logic.PatientInfoManager;
import com.example.demo.logic.PatientMedicalInfoManager;
import com.example.demo.logic.PolicyManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes={DemoApplication.class})
public class InferenceDetectionEngineTest {

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PatientMedicalInfoRepository patientMedicalInfoRepository;

    @Autowired
    private PatientlnfoRepository patientInfoRepository;

    private PatientInfoManager pi;

    private PatientMedicalInfoManager pmi;

    private static final Logger log = LoggerFactory.getLogger(InferenceDetectionEngineTest.class);


    @Bean
    public PatientMedicalInfoManager PatientMedicalInfoManager(
            PatientMedicalInfoRepository patientMedicalInfoRepository) {
        return new PatientMedicalInfoManager(patientMedicalInfoRepository);
    }

    @Bean
    public PatientInfoManager patientInfoManager(PatientlnfoRepository patientInfoRepository) {
        return new PatientInfoManager(patientInfoRepository);
    }

    @Test
    public void noPolicy() {
        patientMedicalInfoRepository.saveAll(Arrays.asList(
                new PatientMedicalInfo(null, "TBD", "Cardiac Arrest", false),
                new PatientMedicalInfo(null, "3", "Brain Aneurysm",false),
                new PatientMedicalInfo(null, "4", "Brain Aneurysm",false)));

        patientInfoRepository.saveAll(Arrays.asList(
                new PatientInfo("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M", false),
                new PatientInfo("Mary Jane", "Oct 22, 2014", "Oct 31, 2014", "F", false),
                new PatientInfo("Patty Patterson", "Oct 24, 2014", "Oct 31, 2014", "F", false)));

        pi = patientInfoManager(patientInfoRepository);
        pmi = PatientMedicalInfoManager(patientMedicalInfoRepository);

        //search with no policy
        pmi.search(null, "4", "Brain Aneurysm");
        List<PatientInfo> result = pi.search("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M");

        assertFalse(result.get(0).isInference());

    }

    @Test
    public void withPolicy() {

        patientMedicalInfoRepository.saveAll(Arrays.asList(
                new PatientMedicalInfo(null, "TBD", "Cardiac Arrest", false),
                new PatientMedicalInfo(null, "3", "Brain Aneurysm",false),
                new PatientMedicalInfo(null, "4", "Brain Aneurysm",false)));

        patientInfoRepository.saveAll(Arrays.asList(
                new PatientInfo("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M", false),
                new PatientInfo("Mary Jane", "Oct 22, 2014", "Oct 31, 2014", "F", false),
                new PatientInfo("Patty Patterson", "Oct 24, 2014", "Oct 31, 2014", "F", false)));

        pi = patientInfoManager(patientInfoRepository);
        pmi = PatientMedicalInfoManager(patientMedicalInfoRepository);


        //new policy
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

    }

    @Test
    public void withPolicyNoInference() {

        patientMedicalInfoRepository.saveAll(Arrays.asList(
                new PatientMedicalInfo(null, "TBD", "Cardiac Arrest", false),
                new PatientMedicalInfo(null, "3", "Brain Aneurysm",false),
                new PatientMedicalInfo(null, "4", "Brain Aneurysm",false)));

        patientInfoRepository.saveAll(Arrays.asList(
                new PatientInfo("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M", false),
                new PatientInfo("Mary Jane", "Oct 22, 2014", "Oct 31, 2014", "F", false),
                new PatientInfo("Patty Patterson", "Oct 24, 2014", "Oct 31, 2014", "F", false)));

        pi = patientInfoManager(patientInfoRepository);
        pmi = PatientMedicalInfoManager(patientMedicalInfoRepository);


        //new policy
        Policy p = new Policy();
        p.setInputColumns(Arrays.asList("patient_medical_info.length_of_stay", "patient_info.date_of_entry",
                "patient_info.date_of_leave"));
        p.setBlockedColumns(Arrays.asList("patient_info.name"));
        p.setRelationship("patient_info.date_of_leave - patient_info.date_of_entry != patient_medical_info.length_of_stay");
        policyRepository.save(p);

        //No inference detected in this search
        pi.search("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M");
        pmi.search(null, "3", "Brain Aneurysm");
        List<PatientInfo> result = pi.search("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M");

        assertFalse(result.get(0).isInference());

    }

    @Test
    public void withPolicyRemoved() {

        patientMedicalInfoRepository.saveAll(Arrays.asList(
                new PatientMedicalInfo(null, "TBD", "Cardiac Arrest", false),
                new PatientMedicalInfo(null, "3", "Brain Aneurysm",false),
                new PatientMedicalInfo(null, "4", "Brain Aneurysm",false)));

        patientInfoRepository.saveAll(Arrays.asList(
                new PatientInfo("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M", false),
                new PatientInfo("Mary Jane", "Oct 22, 2014", "Oct 31, 2014", "F", false),
                new PatientInfo("Patty Patterson", "Oct 24, 2014", "Oct 31, 2014", "F", false)));

        pi = patientInfoManager(patientInfoRepository);
        pmi = PatientMedicalInfoManager(patientMedicalInfoRepository);

        //remove policies
        policyRepository.deleteAll();

        //search with policy removed beforehand
        pi.search("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M");
        pmi.search(null, "4", "Brain Aneurysm");
        List<PatientInfo> result = pi.search("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M");

        assertFalse(result.get(0).isInference());

    }


    @Test
    public void performanceTest() {

        patientMedicalInfoRepository.saveAll(Arrays.asList(
                new PatientMedicalInfo(null, "TBD", "Cardiac Arrest", false),
                new PatientMedicalInfo(null, "3", "Brain Aneurysm",false),
                new PatientMedicalInfo(null, "4", "Brain Aneurysm",false)));

        patientInfoRepository.saveAll(Arrays.asList(
                new PatientInfo("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M", false),
                new PatientInfo("Mary Jane", "Oct 22, 2014", "Oct 31, 2014", "F", false),
                new PatientInfo("Patty Patterson", "Oct 24, 2014", "Oct 31, 2014", "F", false)));

        pi = patientInfoManager(patientInfoRepository);
        pmi = PatientMedicalInfoManager(patientMedicalInfoRepository);

        //remove policies
        policyRepository.deleteAll();

        //search without policy
        Instant start = Instant.now();
        pi.search("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M");
        Instant finish = Instant.now();
        long timeElapsedWithoutPolicy = Duration.between(start, finish).toMillis();

        //new policy
        Policy p = new Policy();
        p.setInputColumns(Arrays.asList("patient_medical_info.length_of_stay", "patient_info.date_of_entry",
                "patient_info.date_of_leave"));
        p.setBlockedColumns(Arrays.asList("patient_info.name"));
        p.setRelationship("patient_info.date_of_leave - patient_info.date_of_entry != patient_medical_info.length_of_stay");
        policyRepository.save(p);

        pmi.search(null, "4", "Brain Aneurysm");

        //search with policy
        start = Instant.now();
        List<PatientInfo> result = pi.search("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M");
        finish = Instant.now();
        long timeElapsedWithPolicy = Duration.between(start, finish).toMillis();

        //less than 2s
        assertTrue(timeElapsedWithPolicy-timeElapsedWithoutPolicy < 2000);

    }

}


*/