package com.example.demo.data.repository;

import java.util.List;

import com.example.demo.data.model.BillingInfo;

public interface CustomBillingInfoRepository {

    List<BillingInfo> customSearch(String accountNumber, String patientAddress, Integer totalMedicalCosts);
    
}
