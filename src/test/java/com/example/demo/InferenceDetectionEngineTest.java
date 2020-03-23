

package com.example.demo;

import com.example.demo.config.GlobalConfiguration;
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

import org.junit.Before;
import org.junit.Test;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "file:src/main/java/com/example/demo/config/LoadData"
})
public class InferenceDetectionEngineTest {

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
    private TestEntityManager entityManager;

    private static final Logger log = LoggerFactory.getLogger(InferenceDetectionEngineTest.class);


    @Before
    public void before(){


        pi = new PatientInfoManager(patientInfoRepository);
        pmi = new PatientMedicalInfoManager(patientMedicalInfoRepository);


        patientInfoRepository.saveAll(Arrays.asList(
                new PatientInfo("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M", false),
                new PatientInfo("Mary Jane", "Oct 22, 2014", "Oct 31, 2014", "F", false),
                new PatientInfo("Patty Patterson", "Oct 24, 2014", "Oct 31, 2014", "F", false)));



        patientMedicalInfoRepository.saveAll(Arrays.asList(
                new PatientMedicalInfo(1L, "TBD", "Cardiac Arrest", false),
                new PatientMedicalInfo(2L, "3", "Brain Aneurysm",false),
                new PatientMedicalInfo(3L, "4", "Brain Aneurysm",false)));



    }

    @Test
    @Order(1)
    public void noPolicy() {

        //search with no policy
        pmi.search(null, "4", "Brain Aneurysm");
        List<PatientInfo> result = pi.search("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M");

        assertFalse(result.get(0).isInference());

    }

    @Test
    @Order(2)
    public void withPolicy() {


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
    @Order(3)
    public void withPolicyNoInference() {


        //No inference detected in this search
        pi.search("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M");
        pmi.search(null, "3", "Brain Aneurysm");
        List<PatientInfo> result = pi.search("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M");

        assertFalse(result.get(0).isInference());

    }

    @Test
    @Order(4)
    public void withPolicyRemoved() {

        //remove policies
        policyRepository.deleteAll();

        //search with policy removed beforehand
        pi.search("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M");
        pmi.search(null, "4", "Brain Aneurysm");
        List<PatientInfo> result = pi.search("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M");

        assertFalse(result.get(0).isInference());

    }


    @Test
    @Order(5)
    public void performanceTest() {

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


