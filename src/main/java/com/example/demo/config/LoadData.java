package com.example.demo.config;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.demo.data.model.PatientInfo;
import com.example.demo.data.model.PatientMedicalInfo;
import com.example.demo.data.model.Policy;
import com.example.demo.data.model.Role;
import com.example.demo.data.model.User;
import com.example.demo.data.repository.PatientMedicalInfoRepository;
import com.example.demo.data.repository.PatientlnfoRepository;
import com.example.demo.data.repository.PolicyRepository;
import com.example.demo.data.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LoadData implements
  ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = LoggerFactory.getLogger(LoadData.class);

    boolean alreadySetup = false;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PatientMedicalInfoRepository patientMedicalInfoRepository;

    @Autowired
    private PatientlnfoRepository patientInfoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
  
        if (alreadySetup) {
            log.info("Already loaded stub data");
            return;
        } else 
            log.info("Populating database with stub data");
        
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

        Role doctorRole = new Role(0, "ROLE_DOCTOR", null);
        Role adminRole = new Role(0, "ROLE_ADMIN", null);

        List<User> users = new ArrayList<>();
        users.add(new User(1, "hasan", LocalDate.now(), null, passwordEncoder.encode("admin"), Arrays.asList(adminRole)));
        users.add(new User(2, "ryan", LocalDate.now(), null, passwordEncoder.encode("admin"), Arrays.asList(adminRole)));
        users.add(new User(3, "jason", LocalDate.now(), null, passwordEncoder.encode("doctor"), Arrays.asList(doctorRole)));

        userRepository.saveAll(null);

        alreadySetup = true;
    }
}