package com.example.demo.data.model;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.metadata.ClassMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@ToString
@Data 
@DiscriminatorValue("patient_medical_info")
@NoArgsConstructor
@AllArgsConstructor
public class PatientMedicalInfo extends SuperTable{

    @Column
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "patient_medical_info_generator")
    @SequenceGenerator(name="patient_medical_info_generator", sequenceName = "patient_medical_info_seq")
    private String patientId;

    @Column
    private String lengthOfStay;

    @Column
    private String reasonOfVisit;

    @Transient
    private boolean inference;


    @Override
    public String getColumnValue(String col){
        switch(col){
            case "patient_id":
                return patientId;
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
        return patientId;
    }

    @Override
    public void setInference(boolean b){
        inference = b;
    }


} 