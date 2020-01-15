package com.example.demo.logic;

import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import com.example.demo.data.model.Table;
import com.example.demo.data.repository.DBLogEntryRepository;
import com.example.demo.data.repository.PatientMedicalInfoRepository;
import com.example.demo.data.repository.PatientlnfoRepository;
import com.example.demo.data.repository.PolicyRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.spel.ast.Operator;
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
    

    public List<PatientInfo> checkInferenceForPatientInfo(List<PatientInfo> resultList, List<String> tablesAndColumnsAccessed) {

        if(!resultList.isEmpty()){
            logger.info("results => " + resultList);
            //1 - First step for the inference detection is to fetch the user 
            String currentUserName = getUser();
            logger.info("getUser() => " + currentUserName);
            
            //2. get policies related to the query
            List<Policy> policies = policyRepository.findDistinctByInputColumnsInAndBlockedColumnsIn(tablesAndColumnsAccessed, tablesAndColumnsAccessed);
            logger.info("get policies, found [" + policies.size() + "] policies => " + policies);

            //3. record a log of the query performed 
            List<String> values = ((List<PatientInfo>)resultList).stream().map(e -> String.valueOf(e.getName())).collect(Collectors.toList());
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
                        logger.info("policy RelationshipOperands=>" + policyRelationshipOperands);
                        Queue<String> policyRelationshipOperators = p.getRelationshipOperators();
                        logger.info("policy RelationshipOperators=>" + policyRelationshipOperators);
                        
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
                            //Ignore logs that are part of the item in focus of the result list, item's values have already been added to logical relationship 
                            if(!entry.getTablesColumnsAccessed().get(0).startsWith(pi.getTableName())){

                                //9. get table columns accessed in each log 
                                List<String> tableColumns = entry.getTablesColumnsAccessed();
                                for(String operand: policyRelationshipOperands){
                                    //ignore if one of the policy input columns is one of the columns of the item in focus of the result list
                                    if(!operand.startsWith(pi.getTableName())){
                                        if(tableColumns.contains(operand)){
                                            for(String id: entry.getIdsAccessed()){
                                                
                                                policyRelationshipOperands.set(policyRelationshipOperands.indexOf(operand), queryRepositories(operand, id));

                                                if(!isInference(policyRelationshipOperators, policyRelationshipOperands)) pi.setInference(false);

                                            }
                                        }
                                    }

                                }           
                            }
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

    private String queryRepositories(String tableColumn, String id){
        logger.info("operand:"+tableColumn);
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

        String operator = operators.remove();
        if(operator == null && operands.size() == 1){
            boolean result = Boolean.parseBoolean(operands.remove(0));
            return result;
        }
        else{
            operands.add(0,evaluateExpression(operators.remove(), operands.remove(0), operands.remove(1)));
            isInference(operators, operands);
        }
        return true;

        
    }

    private String evaluateExpression(String operator, String operand1, String operand2){

        if(isValidDate(operand1) && isValidDate(operand2)){
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
            LocalDate date1 = LocalDate.parse(operand1, dateFormatter);
            LocalDate date2 = LocalDate.parse(operand2, dateFormatter);
            
            switch(operator.trim()){

                case "-":
                    int daysBetween = (int) ChronoUnit.DAYS.between(date1, date2);
                    logger.info("Days between: "+String.valueOf(daysBetween));
                    return String.valueOf(daysBetween);
                case "==":
                    logger.info("Expression is : "+String.valueOf(date1.equals(date2)));
                    return String.valueOf(date1.equals(date2));    
                case "!=":
                    logger.info("Expression is : "+String.valueOf(!date1.equals(date2));
                    return String.valueOf(!date1.equals(date2));                    
                default:
                    return null;
            }

        }
        else if(isInteger(operand1) && isInteger(operand2)){

            int arg1 = Integer.valueOf(operand1);
            int arg2 = Integer.valueOf(operand1);
            switch(operator.trim()){

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
                    logger.info("Expression is : "+String.valueOf(arg1 == arg2));
                    return String.valueOf(arg1 == arg2);    
                case "!=":
                    logger.info("Expression is : "+String.valueOf(arg1 != arg2));
                    return String.valueOf(arg1 != arg2);                   
                default:
                    return null;
            }

        }
        else return null;
    }


    private boolean isValidDate(String inDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
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

    



