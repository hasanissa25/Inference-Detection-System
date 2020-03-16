package com.example.demo.logic;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;

import com.example.demo.data.model.BillingInfo;
import com.example.demo.data.model.DBLogEntry;
import com.example.demo.data.model.DBLogEntry2;
import com.example.demo.data.model.PatientInfo;
import com.example.demo.data.model.PatientMedicalInfo;
import com.example.demo.data.model.Policy;

import com.example.demo.data.model.QueryResult;

import com.example.demo.data.repository.DBLogEntryRepository;
import com.example.demo.data.repository.PatientMedicalInfoRepository;
import com.example.demo.data.repository.BillingInfoRepository;
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
    private PatientMedicalInfoRepository BillingInfoRepository;
    
    @Autowired
    private PatientlnfoRepository patientlnfoRepository;
    
    
    public List<PatientInfo> checkInferenceForPatientInfo(List<PatientInfo> resultList, List<String> tablesAndColumnsAccessed) {
        
        if(!resultList.isEmpty()){
            logger.info("results => " + resultList);
            logger.info("table columns accessed => "+tablesAndColumnsAccessed);
            //1 - First step for the inference detection is to fetch the user 
            String currentUserName = getUser();
            logger.info("getUser() => " + currentUserName);
            
            //2. get policies related to the query
            List<Policy> policies = policyRepository.findDistinctByInputColumnsInAndBlockedColumnsIn(tablesAndColumnsAccessed, tablesAndColumnsAccessed);
            logger.info("get policies, found [" + policies.size() + "] policies => " + policies);
            
            //3. record a log of the query performed 
            List<String> values = resultList.stream().map(e -> String.valueOf(e.getName())).collect(Collectors.toList());
            logger.info("values => " + values);
            DBLogEntry dbLogEntry = new DBLogEntry(null, currentUserName, tablesAndColumnsAccessed, values, LocalDateTime.now());
            
            
            logger.info("recording and saving log => " + dbLogEntry);
            dbLogEntryRepository.save(dbLogEntry);            
            
            //4.for each item in the result list check if it causes potential inference attack
            for(PatientInfo pi : resultList) {
                //set inference to false
                pi.setInference(false);
                logger.info("PatientInfo =>" + pi);
                for(Policy p: policies) {
                    //5. Get the policyInputColumns
                    List<String> policyInputColumns = new ArrayList<>(p.getInputColumns());

                    logger.info("policyInputColumns =>" + policyInputColumns);
                    
                    //6. Parse the logical relationship of the inputColumns
                    ArrayList<String> policyRelationshipOperands = p.getRelationshipOperands();
                    logger.info("policyRelationshipOperands=>" + policyRelationshipOperands);
                    Queue<String> policyRelationshipOperators = p.getRelationshipOperators();
                    logger.info("policyRelationshipOperators=>" + policyRelationshipOperators);
                    
                    //7. Check if one of the inputColumns is part of the item in focus of the result list
                    //if so, add the value of that column into the logical relationship
                    for(String operand: policyRelationshipOperands){
                        String col = operand.split("\\.")[1].trim();
                        String table = operand.split("\\.")[0].trim();
                        if(table.equals(pi.getTableName())){
                            policyRelationshipOperands.set(policyRelationshipOperands.indexOf(operand), pi.getColumn(col));
                        }
                    }
                    
                    //8. Get the logs that have accessed the policyInputColumns
                    List<DBLogEntry> logEntries = dbLogEntryRepository.findDistinctByTablesColumnsAccessedIn(policyInputColumns);
                    logger.info("logEntries =>" + logEntries);
                    
                    //9. iterate through each log

                    for(DBLogEntry entry: logEntries) {
                        logger.info("Log entry=>" + entry);
                        //get table columns accessed in each log 
                        List<String> tableColumnsFromLog = entry.getTablesColumnsAccessed();
                        //Ignore logs that are part of the item in focus of the result list, item's values have already been added to logical relationship 
                        if(!tableColumnsFromLog.get(0).startsWith(pi.getTableName())){
                            List<String> operands; 
                            Queue<String> operators;
                            //10. loop through each id accessed in the log
                            for(String id: entry.getIdsAccessed()){
                                //list of operands for each id/row accessed
                                operands = new ArrayList<String>(policyRelationshipOperands);
                                //queue of operators from the policy relationship
                                operators = new LinkedList<String>(policyRelationshipOperators);
                                //11. loop through each column on the policy input columns
                                for(String operand: policyRelationshipOperands){
                                    //ignore if one of the policy input columns is one of the columns of the item in focus of the result list
                                    //the values of the item's columns have already been added
                                    if(!operand.startsWith(pi.getTableName())){
                                        //12. if table columns accessed in the log contains one of the input policy columns, add the value of the operand to the list
                                        if(tableColumnsFromLog.contains(operand)){
                                            operands.set(operands.indexOf(operand), queryRepositories(operand, id));
                                            
                                        }
                                    }
                                }
                                //using the operands and operators for this row, check if there is an inference detection
                                if(isInference(operators, operands)) pi.setInference(true);  
                                
                            }
                            
                        }
                        
                    }
                    
                }
            }
        }
        
        return resultList;
    }

    // public List<BillingInfo> checkInferenceForBillingInfo(List<BillingInfo> resultList, List<String> tablesAndColumnsAccessed) {
        
    //     boolean policiesFound = false;

    //     //1 - First step for the inference detection is to fetch the user 
    //     String currentUserName = getUser();
    //     logger.info("Step 1: getUser() => " + currentUserName);
        
    //     //2. get policies related to the query
    //     List<Policy> policies = policyRepository.findDistinctByInputColumnsInAndBlockedColumnsIn(tablesAndColumnsAccessed, tablesAndColumnsAccessed);
    //     logger.info("Step 2: get policies, found [" + policies.size() + "] policies => " + policies);
    //     if(!policies.isEmpty()) {
    //         policiesFound = true;
    //     }
        
    //     if(policiesFound) {
    //         //for each item in the result list check if it causes potential inference attack
    //         Map<BillingInfo, Boolean> inferenceDetectionResults = new HashMap<>();
    //         for(BillingInfo pi : resultList) {
    //             for(Policy p: policies) {
    //                 //3. get the input columns that are not part of this query
    //                 List<String> policyInputColumns = new ArrayList<>(p.getInputColumns());
    //                 policyInputColumns.removeIf(x-> tablesAndColumnsAccessed.contains(x));
    //                 //get information from the logs based on the policy input columns
    //                 List<DBLogEntry> logEntries = dbLogEntryRepository.findDistinctByTablesColumnsAccessedInAndUserName(policyInputColumns, currentUserName);
    //                 //logger.info("Logs=>" + logEntries);
    //                 //TODO: if we have duplicate information, then we will process into unique set
    //                 //TODO: for each log entry, check the policy criteria
    //                 for(DBLogEntry entry: logEntries) {
    //                     for(String id: entry.getIdsAccessed()) {
    //                         Integer dailyMedicalCost = patientMedicallnfoRepository.findById(Long.valueOf(id)).get().getDailyMedicalCost();
    //                         String lengthOfStay = patientMedicallnfoRepository.findById(Long.valueOf(id)).get().getLengthOfStay();
    //                         Integer lengthOfStayN = Integer.valueOf(lengthOfStay);
    //                         Integer totalMedicalCosts = pi.getTotalMedicalCosts();
                            
    //                         boolean isInference = false;
    //                         //=========================================================================================
    //                         //TODO: how to make it generic, an idea, implement and replace hardcoded parts
    //                         Map<String, Integer> policyCriteriaInputMap = new HashMap<>();
    //                         policyCriteriaInputMap.put("patient_medical_info.length_of_stay", lengthOfStayN);
    //                         policyCriteriaInputMap.put("patient_medical_info.daily_medical_cost", dailyMedicalCost);
    //                         policyCriteriaInputMap.put("billing_info.total_medical_costs", totalMedicalCosts);
    //                         //boolean isInference = p.processCriteria(policyCriteriaInputMap);
    //                         //=========================================================================================
    //                         // Hard coded part
    //                         //=========================================================================================
    //                         if("TBD".equals(totalMedicalCosts) || "TBD".equals(dailyMedicalCost)) {
    //                             continue; //ignore this and continue to the next one
    //                         }
    //                         else {
    //                             //int lengthOfStayN = Integer.valueOf(lengthOfStay);
    //                             int bill = lengthOfStayN * dailyMedicalCost;
    //                             isInference = bill == totalMedicalCosts; //the hard coded criteria, if they are equal
    //                             logger.info("pi.accountNumber=" + pi.getAccountNumber() + ", pmi.id=" + id +", bill =" + bill + ", total medical costs=" + totalMedicalCosts + ", inference=" + isInference);
    //                         }
    //                         //=========================================================================================
                            
    //                         //TODOif it matches, mark it as reference attack
    //                         if(isInference) {
    //                             pi.setInference(true);
    //                             break;
    //                         }
    //                     }
    //                 }
                    
    //             }
    //         }
    //     }

    //     //last step. record a log of the query performed (TODO: add inference logic)
    //     List<String> values = resultList.stream().map(e -> String.valueOf(e.getAccountNumber())).collect(Collectors.toList());
    //     DBLogEntry dbLogEntry = new DBLogEntry(null, currentUserName, tablesAndColumnsAccessed, values, LocalDateTime.now());
        
    //     logger.info("Step last: recording log => " + dbLogEntry);
    //     dbLogEntryRepository.save(dbLogEntry);

    //     return resultList;
    // }

    public List<PatientMedicalInfo> checkInferenceForPatientMedicalInfo(List<PatientMedicalInfo> resultList, List<String> tablesAndColumnsAccessed) {
        if(!resultList.isEmpty()){
            logger.info("results => " + resultList);
            //1 - First step for the inference detection is to fetch the user 
            String currentUserName = getUser();
            logger.info("getUser() => " + currentUserName);
            
            //2. get policies related to the query
            List<Policy> policies = policyRepository.findDistinctByInputColumnsInAndBlockedColumnsIn(tablesAndColumnsAccessed, tablesAndColumnsAccessed);
            logger.info("get policies, found [" + policies.size() + "] policies => " + policies);
            
            //3. record a log of the query performed 
            List<String> values = resultList.stream().map(e -> String.valueOf(e.getId())).collect(Collectors.toList());
            logger.info("values => " + values);
            DBLogEntry dbLogEntry = new DBLogEntry(null, currentUserName, tablesAndColumnsAccessed, values, LocalDateTime.now());
            
            
            logger.info("recording and saving log => " + dbLogEntry);
            dbLogEntryRepository.save(dbLogEntry);            
            
            //4.for each item in the result list check if it causes potential inference attack
            for(PatientMedicalInfo pi : resultList) {
                //set inference to false
                pi.setInference(false);
                logger.info("PatientInfo =>" + pi);
                for(Policy p: policies) {
                    //5. Get the policyInputColumns
                    List<String> policyInputColumns = new ArrayList<>(p.getInputColumns());
                    logger.info("policyInputColumns =>" + policyInputColumns);
                    //6. Parse the logical relationship of the inputColumns
                    ArrayList<String> policyRelationshipOperands = p.getRelationshipOperands();
                    logger.info("policyRelationshipOperands=>" + policyRelationshipOperands);
                    Queue<String> policyRelationshipOperators = p.getRelationshipOperators();
                    logger.info("policyRelationshipOperators=>" + policyRelationshipOperators);
                    
                    //7. Check if one of the inputColumns is part of the item in focus of the result list
                    //if so, add the value of that column into the logical relationship
                    for(String operand: policyRelationshipOperands){
                        String col = operand.split("\\.")[1].trim();
                        String table = operand.split("\\.")[0].trim();
                        if(table.equals(pi.getTableName())){
                            policyRelationshipOperands.set(policyRelationshipOperands.indexOf(operand), pi.getColumnValue(col));
                        }
                    }
                    
                    //8. Get the logs that have accessed the policyInputColumns
                    List<DBLogEntry> logEntries = dbLogEntryRepository.findDistinctByTablesColumnsAccessedIn(policyInputColumns);
                    logger.info("logEntries =>" + logEntries);
                    
                    //9. iterate through each log
                    for(DBLogEntry entry: logEntries) {
                        logger.info("Log entry=>" + entry);
                        //get table columns accessed in each log 
                        List<String> tableColumnsFromLog = entry.getTablesColumnsAccessed();
                        //Ignore logs that are part of the item in focus of the result list, item's values have already been added to logical relationship 
                        if(!tableColumnsFromLog.get(0).startsWith(pi.getTableName())){
                            
                            //10. loop through each id accessed in the log
                            for(String id: entry.getIdsAccessed()){
                                //list of operands for each id/row accessed
                                List<String> operands = new ArrayList<String>(policyRelationshipOperands);
                                //queue of operators from the policy relationship
                                Queue<String> operators = new LinkedList<String>(policyRelationshipOperators);
                                //11. loop through each column on the policy input columns
                                for(String operand: policyRelationshipOperands){
                                    //ignore if one of the policy input columns is one of the columns of the item in focus of the result list
                                    //the values of the item's columns have already been added
                                    if(!operand.startsWith(pi.getTableName())){
                                        //12. if table columns accessed in the log contains one of the input policy columns, add the value of the operand to the list
                                        if(tableColumnsFromLog.contains(operand)){
                                            operands.set(operands.indexOf(operand), queryRepositories(operand, id));
                                            
                                        }
                                    }
                                }
                                //using the operands and operators for this row, check if there is an inference detection
                                if(isInference(operators, operands)) pi.setInference(true);  
                                
                            }
                            
                        }
                        
                    }
                    
                }
            }
        }
        
        return resultList;
    }

    // public List<BillingInfo> checkInferenceForBillingInfo(List<BillingInfo> resultList, List<String> tablesAndColumnsAccessed) {
    //     //1 - First step for the inference detection is to fetch the user 
    //     String currentUserName = getUser();
    //     logger.info("Step 1: getUser() => " + currentUserName);
        
    //     List<String> values = resultList.stream().map(e -> String.valueOf(e.getAccountNumber())).collect(Collectors.toList());
    //     DBLogEntry dbLogEntry = new DBLogEntry(null, currentUserName, tablesAndColumnsAccessed, values, LocalDateTime.now());
    //     logger.info("Step last: recording log => " + dbLogEntry);
    //     dbLogEntryRepository.save(dbLogEntry);
    
    //     return resultList;
    // } //might be unnessecary

    
    //We get the user if they are logged in. If they are not logged in we return null.
    private String getUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }
        else return null;
    }
    
    private String queryRepositories(String tableColumn, String id){
        logger.info("id queried:"+id);
        String table = tableColumn.split("\\.")[0];
        String col = tableColumn.split("\\.")[1];
        
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
                logger.info("result of query:"+patientMedicallnfoRepository.findById(Long.valueOf(id)).get().getLengthOfStay());
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
    
    private boolean isInference(Queue<String> operators, List<String> operands){
        //base case
        if(operators.isEmpty() && operands.size() < 2){
            if(operands.get(0) == null || operands.isEmpty()) return false;
            boolean result = Boolean.parseBoolean(operands.remove(0));
            logger.info("IS-INFERENCE: The result of the inference detection is =>" + result);
            return result;
        }else if(!operators.isEmpty() && operands.size() >= 2){
            
            String operator = operators.remove();
            String operand1 = operands.remove(0);
            String operand2 = operands.remove(0);
            logger.info("IS-INFERENCE: OPERATOR =>" + operator);
            logger.info("IS-INFERENCE: OPERAND1 =>" + operand1);
            logger.info("IS-INFERENCE: OPERAND2 =>" + operand2);
            
            if(operator == null || operand1 == null || operand2 == null){
                logger.info("IS-INFERENCE: Cannot evaluate expression, data types not compatible");
                return false;
            }
            else{
                operands.add(0,evaluateExpression(operator, operand1, operand2));
                return isInference(operators, operands);
            }
        }
        else{
            logger.info("IS-INFERENCE: Cannot evaluate expression, policy relationship is invalid");
            return false;
        } 
        
    }
    
    private String evaluateExpression(String operator, String operand1, String operand2){
        
        boolean result;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        int daysBetween, arg1, arg2;
        
        if(isValidDate(operand1) && isValidDate(operand2)){
            logger.info("IS DATE");
            
            LocalDate date1 = LocalDate.parse(operand1, dateFormatter);
            LocalDate date2 = LocalDate.parse(operand2, dateFormatter);
            
            switch(operator.trim()){
                
                case "-":
                daysBetween = (int) ChronoUnit.DAYS.between(date1, date2);
                if (daysBetween < 0){
                    daysBetween = (int) ChronoUnit.DAYS.between(date2, date1);
                }
                logger.info("Days between: "+String.valueOf(daysBetween));
                return String.valueOf(daysBetween);
                case "==":
                if(date1.equals(date2))result = true;
                else result = false;
                
                logger.info("Expression is : "+Boolean.toString(result));
                return Boolean.toString(result);
                
                case "!=":
                if(!date1.equals(date2))result = true;
                else result = false;
                
                logger.info("Expression is : "+Boolean.toString(result));
                return Boolean.toString(result);                  
                default:
                return null;
            }
            
        }
        else if(isInteger(operand1) && isInteger(operand2)){
            
            logger.info("IS INTEGER");
            
            arg1 = Integer.valueOf(operand1);
            arg2 = Integer.valueOf(operand2);
            switch(operator.trim()) {
                
                case "-":
                logger.info("Difference is: "+String.valueOf(arg1 - arg2));
                return String.valueOf(arg1 - arg2);
                case "+":
                logger.info("Sum is: "+String.valueOf(arg1 + arg2));
                return String.valueOf(arg1 + arg2);
                case "/":
                logger.info("Division is: "+String.valueOf(arg1 / arg2));
                return String.valueOf(arg1 / arg2);
                case "*":
                logger.info("Multiplication is: "+String.valueOf(arg1 * arg2));
                return String.valueOf(arg1 * arg2);
                case "==":
                
                if(arg1 != arg2)result = true;
                else result = false;
                
                logger.info("Expression is : "+Boolean.toString(result));
                return Boolean.toString(result);
                case "!=":
                if(arg1 == arg2)result = true;
                else result = false;
                logger.info("Expression is : "+Boolean.toString(result));
                return Boolean.toString(result);       
                
                default:
                return null;
            }
            
        }
        else return null;
    }
    
    
    private boolean isValidDate(String inDate) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        try{
            LocalDate.parse(inDate, dateFormatter);
        } catch (DateTimeParseException pe) {
            return false;
        }
        return true;
        
    }

    
    private boolean isInteger(String input) {
        try {
            Integer.parseInt( input );
            return true;
        }
        catch( Exception e ) {
            return false;
        }
    }
    

}





