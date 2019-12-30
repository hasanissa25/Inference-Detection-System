package com.example.demo.data.repository;

import java.util.List;

import com.example.demo.data.model.PatientInfo;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientlnfoRepository extends JpaRepository<PatientInfo, Long> {

	List<PatientInfo> findByNameIgnoreCaseOrDateOfEntryOrDateOfLeaveOrGenderIgnoreCase(String patientName, String dateOfEntry,
			String dateOfLeave, String gender);
}
