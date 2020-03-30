// package com.example.demo.data.model;

// import javax.persistence.Column;
// import javax.persistence.Entity;
// import javax.persistence.GeneratedValue;
// import javax.persistence.GenerationType;
// import javax.persistence.Id;
// import javax.persistence.SequenceGenerator;
// import javax.persistence.Transient;

// import lombok.AllArgsConstructor;
// import lombok.Data;
// import lombok.NoArgsConstructor;
// import lombok.ToString;

// @Entity
// @ToString
// @Data
// @NoArgsConstructor
// @AllArgsConstructor
// public class BillingInfo {
//     // @Id
//     // @Column(name = "AccountNumber", updatable = false, nullable = false)
//     // //@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "billing_info_generator")
//     // //@SequenceGenerator(name="billing_info_generator", sequenceName = "billing_info_seq")
    
//     // private String accountNumber;
//     // private String patientAddress;
//     // private Integer totalMedicalCosts; 
//     // @Transient
//     // private boolean inference;

//     @Id
//     private String accountNumber;
//     private String patientAddress;
//     private Integer totalMedicalCosts; 
//     @Transient
//     private boolean inference;
// } 



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
public class BillingInfo{// extends Table{
    @Id 
    @Column(name = "AccountNumber", updatable = false, nullable = false)
    //@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "billing_info_generator")
    //@SequenceGenerator(name="billing_info_generator", sequenceName = "billing_info_seq")
    private String accountNumber;
    private String patientAddress; 
    private Integer totalMedicalCosts; 
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
                this.accountNumber = val;
                break;
            case "patient_address":
                this.patientAddress = val;
                break;
            case "total_medical_costs":
                if (val.equals("Not Authorized")) {
                    this.totalMedicalCosts = -1;
                } else {
                    this.totalMedicalCosts = Integer.parseInt(val);
                }
                break;
        }
    }

    // @Override
    public String getTableName() {
        return "billing_info";
    }

    // @Override
    public String getId() {
        return String.valueOf(accountNumber);
    }

    public static String[] getColumnNames() {
        String[] tableColumns = {"account_number", "patient_address","total_medical_costs"};
        return tableColumns;
    }
} 