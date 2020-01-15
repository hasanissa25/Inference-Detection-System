package com.example.demo.logic;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.example.demo.data.model.BillingInfo;
import com.example.demo.data.repository.BillingInfoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

public class BillingInfoManager {

    
    private BillingInfoRepository billingInfoRepository;

    @Autowired
    private InferenceDetectionEngine inferenceDetectionEngine;
    
    @PersistenceContext
    private EntityManager entityManager;

    public BillingInfoManager(BillingInfoRepository patientMedicalnfoRepository) {
        this.billingInfoRepository = patientMedicalnfoRepository;
    }

    public List<BillingInfo> search(int accountNumber, String patientAddress, int totalMedicalCosts) {
        List<BillingInfo> results = null;
        if(accountNumber == 0 && StringUtils.isEmpty(patientAddress) && totalMedicalCosts == 0)
            results = billingInfoRepository.findAll();
        else 
            results = billingInfoRepository.findByNameIgnoreCaseOrDateOfEntryOrDateOfLeaveOrGenderIgnoreCase(accountNumber, patientAddress, totalMedicalCosts);
        //results = inferenceDetectionEngine.checkInferenceForBillingInfo(results, Arrays.asList("billing_info.account_number", "billing_info.patient_address", "billing_info.total_medical_costs"));
        return results;
    }

}
