package com.example.demo.data.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Transient;
import javax.persistence.criteria.CriteriaBuilder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@ToString
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingInfo {
    @Id
    @Column(name = "AccountNumber", updatable = false, nullable = false)
    //@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "billing_info_generator")
    //@SequenceGenerator(name="billing_info_generator", sequenceName = "billing_info_seq")
    private int accountNumber;
    private String patientAddress;
    private int totalMedicalCosts;
    @Transient
    private boolean inference;

    // @Override
    public String getColumnValue(String col){
        switch(col){
            case "account_number":
                return String.valueOf(accountNumber);
            case "patient_address":
                return patientAddress;
            case "total_medical_costs":
                return String.valueOf(totalMedicalCosts);
            default:
                return null;
        }
    }

    public void setByColumn(String col, String val) {
        switch(col){
            case "account_number":
                this.accountNumber = Integer.valueOf(val);
                break;
            case "patient_address":
                this.patientAddress=val;
                break;
            case "total_medical_costs":
                this.totalMedicalCosts= Integer.valueOf(val);
                break;

        }
    }


    // @Override
    public String getId() {
        return String.valueOf(accountNumber);
    }

    public void setInference(boolean b){
        this.inference=b;
    }

    // @Override
    public String getTableName() {
        return "billing_info";
    }


}


