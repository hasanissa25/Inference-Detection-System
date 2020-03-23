package com.example.demo.data.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
//@EqualsAndHashCode(callSuper=false)
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PatientInfo{// extends Table{

    @Id
    private String name;
    private String dateOfEntry;
    private String dateOfLeave; 
    private String gender;
    @Transient
    private boolean inference;

   // @Override
    public String getColumnValue(String col){
        switch(col){

            case "date_of_entry":
                return dateOfEntry;
            case "date_of_leave":
                return dateOfLeave;
            case "name":
                return name;
            case "gender":
                return gender;
            default:
                return null;
        }
    }

    public void setByColumn(String col, String val) {
        switch(col){
            case "date_of_entry":
                this.dateOfEntry = val;
                break;
            case "date_of_leave":
                this.dateOfLeave = val;
                break;
            case "name":
                this.name = val;
                break;
            case "gender":
                this.gender = val;
                break;
        }
    }

   // @Override
    public String getTableName() {
        return "patient_info";
    }

    // @Override
    public String getId() {
        return name;
    }

    
} 