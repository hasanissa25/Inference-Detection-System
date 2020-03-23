package com.example.demo.data.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Data 
//@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
@AllArgsConstructor
public class PatientMedicalInfo{// extends Table{
    @Id 
    @Column(name = "patientId", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "patient_medical_info_generator")
    @SequenceGenerator(name="patient_medical_info_generator", sequenceName = "patient_medical_info_seq")
    private Long patientId;
    private String lengthOfStay;
    private String reasonOfVisit; 
    private Integer dailyMedicalCost; 
    @Transient
    private boolean inference;


    // @Override
    public String getColumnValue(String col){
        switch(col){
            case "patient_id":
                return String.valueOf(patientId);
            case "length_of_stay":
                return lengthOfStay;
            case "reason_of_visit":
                return reasonOfVisit;
            case "daily_medical_cost":
                return String.valueOf(dailyMedicalCost);
            default:
                return null;
        }
    }

    public void setByColumn(String col, String val) {
        switch(col){
            case "patient_id":
                this.patientId = -1L;   //TODO
                break;
            case "length_of_stay":
                this.lengthOfStay = val;
                break;
            case "reason_of_visit":
                this.reasonOfVisit = val;
            case "daily_medical_cost":
                this.dailyMedicalCost = Integer.valueOf(val);
                break;

        }
    }

    // @Override
    public String getTableName() {
        return "patient_medical_info";
    }

    // @Override
    public String getId() {
        return String.valueOf(patientId);
    }


} 