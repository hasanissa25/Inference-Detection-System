package com.example.demo.data.repository;

import java.util.List;

import com.example.demo.data.model.BillingInfo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingInfoRepository extends JpaRepository<BillingInfo, Long> {

	List<BillingInfo> findByNameIgnoreCaseOrDateOfEntryOrDateOfLeaveOrGenderIgnoreCase(long accountNumber, String patientAdrress,
			int totalMedicalCosts);
}
