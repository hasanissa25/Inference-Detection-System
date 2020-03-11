package com.example.demo.data.model;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.metadata.ClassMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Entity
@ToString
@Data 
@DiscriminatorValue("patient_medical_info")
@NoArgsConstructor
@AllArgsConstructor
public class PatientMedicalInfo extends SuperTable{

    @Column
    private String patientId;

    @Column
    private String lengthOfStay;

    @Column
    private String reasonOfVisit;

    @Transient
    private boolean inference;

    @PrePersist
    public void initializeUUID() {
        if (patientId == null) {
            patientId = (UUID.randomUUID().toString().replace("-", ""));
        }
    }


    @Override
    public String getColumnValue(String col){
        switch(col){
            case "patient_id":
                return String.valueOf(patientId);
            case "length_of_stay":
                return lengthOfStay;
            case "reason_of_visit":
                return reasonOfVisit;
            default:
                return null;
        }
    }

    public void setByColumn(String col, String val) {
        switch(col){
            case "patient_id":
                this.patientId = val;
                break;
            case "length_of_stay":
                this.lengthOfStay = val;
                break;
            case "reason_of_visit":
                this.reasonOfVisit = val;
                break;
        }
    }

    @Override
    public String getTableName() {
        return "patient_medical_info";
    }

    @Override
    public String[] getColumnNames(){
        return new String[]{"patient_id","length_of_stay","reason_of_visit"};
    }

    @Override
    public String getId() {
        return String.valueOf(patientId);
    }

    @Override
    public void setInference(boolean b){
        inference = b;
    }


} 