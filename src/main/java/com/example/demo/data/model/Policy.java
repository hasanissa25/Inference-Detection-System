package com.example.demo.data.model;

import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Policy {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "policy_generator")
    @SequenceGenerator(name="policy_generator", sequenceName = "policy_seq")
    private Long id;
    @ElementCollection
    private List<String> inputColumns;
    @ElementCollection 
    private List<String> blockedColumns;
    private String relationship;

    private final static Logger logger = LoggerFactory.getLogger(Policy.class);

    public boolean processCriteria(DBLogEntry entry) {
        List<String> entryColumns = entry.getTablesColumnsAccessed();
        logger.info("entrycolumns:" + entryColumns);
        logger.info("inputColumns:" + inputColumns);
        parseRelationship();
        if(entryColumns.containsAll(inputColumns)){
            parseRelationship();
            //entryColumns.get(entryColumns.indexOf())

        }
        return true;
    }
    
    private void parseRelationship(){
        relationship = "patient_info.date_of_entry - patient_info.date_of_leave != patient_medical_info.length_of_stay";
        relationship = relationship.trim();
        String[] tokens = relationship.split("(?=\\W)");
        //String[] tokens = relationship.split("[-+*/=]");    
        logger.info("tokens=>"+tokens);
        for(String token:tokens)  
        {

        }   
    }

}
