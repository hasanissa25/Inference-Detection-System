package com.example.demo.config;

import java.time.LocalDate;
import java.util.Arrays;

import javax.annotation.PostConstruct;

import com.example.demo.data.model.PatientInfo;
import com.example.demo.data.model.PatientMedicalInfo;
import com.example.demo.data.model.Policy;
import com.example.demo.data.model.User;
import com.example.demo.data.repository.PatientMedicalInfoRepository;
import com.example.demo.data.repository.PatientlnfoRepository;
import com.example.demo.data.repository.PolicyRepository;
import com.example.demo.data.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LoadData {

    private static final Logger log = LoggerFactory.getLogger(LoadData.class);

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PatientMedicalInfoRepository patientMedicalInfoRepository;

    @Autowired
    private PatientlnfoRepository patientInfoRepository;

    @Autowired
    private UserRepository userRepository;

    @PostConstruct
    public void run(){
        log.info("Creating stub data into IFS database");
        Policy p = new Policy();
        p.setInputColumns(Arrays.asList("patient_medical_info.length_of_stay", "patient_info.date_of_entry", "patient_info.date_of_leave"));
        p.setBlockedColumns(Arrays.asList("patient_info.name"));
        policyRepository.save(p);


        patientMedicalInfoRepository.saveAll(Arrays.asList(
        new PatientMedicalInfo(null, "TBD", "Cardiac Arrest"),
        new PatientMedicalInfo(null, "3", "Brain Aneurysm"),
        new PatientMedicalInfo(null, "2", "Brain Aneurysm"),
        new PatientMedicalInfo(null, "4", "Cardiac Arrest"),
        new PatientMedicalInfo(null, "2", "Brain Aneurysm"),
        new PatientMedicalInfo(null, "TBD", "Brain Aneurysm"),
        new PatientMedicalInfo(null, "9", "Cardiac Arrest"),
        new PatientMedicalInfo(null, "7", "Cardiac Arrest")
        ));

        patientInfoRepository.saveAll(Arrays.asList(
            new PatientInfo("John Smith", "Oct 27, 2014", "Oct 31, 2014", "M", false),
            new PatientInfo("Mary Jane", "Oct 22, 2014", "Oct 31, 2014", "F", false),
            new PatientInfo("Patty Patterson", "Oct 24, 2014", "Oct 31, 2014", "F", false),
            new PatientInfo("Jimmy Jistle", "Oct 28, 2014", "Oct 31, 2014", "M", false),
            new PatientInfo("Tony Tiger", "Oct 29, 2014", "Oct 31, 2014", "M", false),
            new PatientInfo("Chris Campbell", "Oct 29, 2014", "Oct 31, 2014", "M", false),
            new PatientInfo("Fiona Fastener", "Oct 25, 2014", "TBD", "F", false),
            new PatientInfo("Horus Harvey", "Oct 20, 2014", "TBD", "M", false)
            ));

        userRepository.saveAll(Arrays.asList(
            new User(1, LocalDate.now(), LocalDate.now(), "Ryan", "123456"),
            new User(2,LocalDate.now(), LocalDate.now(), "Calvin", "564321"),
            new User(3,LocalDate.now(), LocalDate.now(), "Sasha", "123456789")
        ));
    }
}