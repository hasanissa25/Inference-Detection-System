package com.example.demo.data.model;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@DiscriminatorValue("patient_info")
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PatientInfo extends SuperTable{

    @Column
    private String name;
    @Column
    private String dateOfEntry;
    @Column
    private String dateOfLeave;
    @Column
    private String gender;
    @Transient
    private boolean inference;

    @Override
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

    @Override
    public String getTableName() {
        return "patient_info";
    }

    @Override
    public String[] getColumnNames() {
        return new String[]{"name", "date_of_entry", "date_of_leave", "gender"};
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public void setInference(boolean b){
        inference = b;
    }
} 