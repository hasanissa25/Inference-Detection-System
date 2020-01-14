package com.example.demo.logic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.demo.data.model.DBLogEntry;
import com.example.demo.data.model.PatientInfo;
import com.example.demo.data.model.PatientMedicalInfo;
import com.example.demo.data.model.Policy;
import com.example.demo.data.repository.DBLogEntryRepository;
import com.example.demo.data.repository.PatientMedicalInfoRepository;
import com.example.demo.data.repository.PatientlnfoRepository;
import com.example.demo.data.repository.PolicyRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class InferenceDetectionEngine {

    private final static Logger logger = LoggerFactory.getLogger(InferenceDetectionEngine.class);

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private DBLogEntryRepository dbLogEntryRepository;
    
    @Autowired
    private PatientMedicalInfoRepository patientMedicallnfoRepository;

    @Autowired
    private PatientlnfoRepository patientlnfoRepository;
    

    public <T> List<T> checkInferenceForPatientInfo(List<T> resultList, List<String> tablesAndColumnsAccessed) {
        

        if(!resultList.isEmpty()){
            logger.info("results => " + resultList);
            //1 - First step for the inference detection is to fetch the user 
            String currentUserName = getUser();
            logger.info("Step 1: getUser() => " + currentUserName);
            
            //2. get policies related to the query
            List<Policy> policies = policyRepository.findDistinctByInputColumnsInAndBlockedColumnsIn(tablesAndColumnsAccessed, tablesAndColumnsAccessed);
            logger.info("Step 2: get policies, found [" + policies.size() + "] policies => " + policies);


            if(resultList.get(0) instanceof PatientInfo){

                //last step. record a log of the query performed (TODO: add inference logic)
                List<String> values = ((List<PatientInfo>)resultList).stream().map(e -> String.valueOf(e.getName())).collect(Collectors.toList());
                logger.info("values => " + values);
                DBLogEntry dbLogEntry = new DBLogEntry(null, currentUserName, tablesAndColumnsAccessed, values, LocalDateTime.now());
                
                logger.info("Step last: recording log => " + dbLogEntry);
                //NOTE: the save opertion also resulted into updating the patient info data retrieved with modification to the inference flag (deprecated), so hibernate picks up changes automatically. be careful of this to avoid unwanted changes to database
                dbLogEntryRepository.save(dbLogEntry);
            

                //for each item in the result list check if it causes potential inference attack
                //Map<PatientInfo, Boolean> inferenceDetectionResults = new HashMap<>();
                for(PatientInfo pi : (List<PatientInfo>) resultList) {
                    logger.info("patient=>" + pi);
                    for(Policy p: policies) {
                    //3. get the input columns that are not part of this query
                    List<String> policyInputColumns = new ArrayList<>(p.getInputColumns());
                    logger.info("policyInputColumns=>" + policyInputColumns);
                    Queue<String> policyRelationshipOperators = p.getRelationshipOperators();
                    
                    //policyInputColumns.removeIf(x-> tablesAndColumnsAccessed.contains(x));
                    //get information from the logs based on the policy input columns
                    List<DBLogEntry> logEntries = dbLogEntryRepository.findDistinctByTablesColumnsAccessedIn(policyInputColumns);
                    
                    Map<List<String>, List<String>> columnAndID = new HashMap<>();
                   
                    for(DBLogEntry entry: logEntries) {
                        logger.info("Log entry=>" + entry);
                        if(!entry.getTablesColumnsAccessed().get(0).startsWith("patient_info")){

                            //columnAndID.put(entry.getTablesColumnsAccessed(), entry.getIdsAccessed());
                            List<String> columns = entry.getTablesColumnsAccessed();
                            for(String operand: policyInputColumns){
                                    if(columns.contains(operand)){
                                        
                                    }
                            }
                        
                        }
                    }



                    
                        
                    
                        

                       // if(!p.processCriteria(entry)){
                         //   pi.setInference(true);
                            //logger.info("pi.name=" + pi.getName() + ", pmi.id=" + id +", days b/w=" + daysBetween + ", length of Stay=" + lengthOfStayN + ", inference=" + isInference);
                         //   break;
                       // }
                        /*
                        for(String id: entry.getIdsAccessed()) {
                            String lengthOfStay = patientMedicallnfoRepository.findById(Long.valueOf(id)).get().getLengthOfStay();
                            String dateOfEntry = pi.getDateOfEntry();
                            String dateOfLeave = pi.getDateOfLeave();
                            
                            boolean isInference = false;
                            //=========================================================================================
                            //TODO: how to make it generic, an idea, implement and replace hardcoded parts
                            Map<String, String> policyCriteriaInputMap = new HashMap<>();
                            policyCriteriaInputMap.put("patient_medical_info.length_of_stay", lengthOfStay);
                            policyCriteriaInputMap.put("patient_info.date_of_entry", dateOfEntry);
                            policyCriteriaInputMap.put("patient_info.date_of_leave", dateOfLeave);
                            //boolean isInference = p.processCriteria(policyCriteriaInputMap);
                            //=========================================================================================
                            // Hard coded part
                            //=========================================================================================
                            if("TBD".equals(lengthOfStay) || "TBD".equals(dateOfLeave)) {
                                continue; //ignore this and continue to the next one
                            }
                            else {
                                int lengthOfStayN = Integer.valueOf(lengthOfStay);
                                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
                                LocalDate dateOfEntryN = LocalDate.parse(dateOfEntry, dateFormatter);
                                LocalDate dateOfLeaveN = LocalDate.parse(dateOfLeave, dateFormatter);
                                int daysBetween = (int) ChronoUnit.DAYS.between(dateOfEntryN, dateOfLeaveN);
                                isInference = daysBetween == lengthOfStayN; //the hard coded criteria, if they are equal
                                logger.info("pi.name=" + pi.getName() + ", pmi.id=" + id +", days b/w=" + daysBetween + ", length of Stay=" + lengthOfStayN + ", inference=" + isInference);
                            }
                            //=========================================================================================
                            */
                        }
                    }
                    
                }
            }
        

    

        return resultList;
    }

    public List<PatientMedicalInfo> checkInferenceForPatientMedicalInfo(List<PatientMedicalInfo> resultList, List<String> tablesAndColumnsAccessed) {
        //1 - First step for the inference detection is to fetch the user 
        String currentUserName = getUser();
        logger.info("Step 1: getUser() => " + currentUserName);
        
        List<String> values = resultList.stream().map(e -> String.valueOf(e.getPatientId())).collect(Collectors.toList());
        DBLogEntry dbLogEntry = new DBLogEntry(null, currentUserName, tablesAndColumnsAccessed, values, LocalDateTime.now());
        logger.info("Step last: recording log => " + dbLogEntry);
        dbLogEntryRepository.save(dbLogEntry);
    
        return resultList;
    }

    //We get the user if they are logged in. If they are not logged in we return null.
    private String getUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!(authentication instanceof AnonymousAuthenticationToken)) {
                return authentication.getName();
            }
            else return null;
    }

    private String getColumnValue(String operand, List<String> ids){
        logger.info("operand:"+operand);
        String table = operand.split("\\.")[0];
        String col = operand.split("\\.")[1];
        

        for(String id: ids){
            switch(table){
                
                case "patient_info":
                    
                    switch(col){
                        case "name":
                            return patientlnfoRepository.findByName(id).getName();                  
                        case "date_of_entry":
                            return patientlnfoRepository.findByName(id).getDateOfEntry();
                        case "date_of_leave":
                            return patientlnfoRepository.findByName(id).getDateOfLeave();
                        case "gender":
                            return patientlnfoRepository.findByName(id).getGender();
                            
                        default:
                            return null;
                    }

                case "patient_medical_info":

                    switch(col){
                        case "patient_id":
                            return patientMedicallnfoRepository.findById(Long.valueOf(id)).get().getPatientId().toString();         
                        case "length_of_stay":
                            return patientMedicallnfoRepository.findById(Long.valueOf(id)).get().getLengthOfStay();
                        case "reason_of_visit":
                            return patientMedicallnfoRepository.findById(Long.valueOf(id)).get().getReasonOfVisit();
                        default:
                            return null;
                    }
                    
                default:
                    return null;

            }

        }
        return null;
    }
    


}
