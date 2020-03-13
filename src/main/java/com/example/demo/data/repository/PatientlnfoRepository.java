package com.example.demo.data.repository;

import java.util.List;

import com.example.demo.data.model.PatientInfo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PatientlnfoRepository extends JpaRepository<PatientInfo, String> {

    @Query("from PatientInfo")
	List<PatientInfo> findByNameIgnoreCaseOrDateOfEntryOrDateOfLeaveOrGenderIgnoreCase(String patientName, String dateOfEntry,
            String dateOfLeave, String gender);

    PatientInfo findByName(String name);

}
