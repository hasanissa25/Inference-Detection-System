package com.example.demo.logic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


import com.example.demo.data.model.*;

import com.example.demo.data.model.Policy.ResponseObject;
import com.example.demo.data.repository.DBLogEntryRepository;
import com.example.demo.data.repository.PatientMedicalInfoRepository;
import com.example.demo.data.repository.PatientlnfoRepository;

import com.example.demo.data.repository.BillingInfoRepository;

import com.example.demo.data.repository.PolicyRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import javax.swing.*;

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
    private BillingInfoRepository billingInfoRepository;

    @Autowired
    private PatientlnfoRepository patientlnfoRepository;

    private Set<String> allLogTableColumns = new HashSet<>();

    private Set<String> allLogIDAccessed = new HashSet<>();


    Map<String, ArrayList<String>> columnValuesMap;

    public List<PatientInfo> checkInferenceForPatientInfo(List<PatientInfo> resultList,
                                                          List<String> tablesAndColumnsAccessed) {

        if (!resultList.isEmpty()) {
            logger.info("results => " + resultList);
            logger.info("table columns accessed => " + tablesAndColumnsAccessed);
            // 1 - First step for the inference detection is to fetch the user
            String currentUserName = getUser();
            logger.info("getUser() => " + currentUserName);

            // 2. get policies related to the query

            List<Policy> policies = policyRepository.findAll();

            logger.info("get policies, found [" + policies.size() + "] policies => " + policies);

            // 3. record a log of the query performed
            List<String> values = resultList.stream().map(e -> String.valueOf(e.getId()))
                    .collect(Collectors.toList());
            logger.info("values => " + values);
            DBLogEntry dbLogEntry = new DBLogEntry(null, currentUserName, tablesAndColumnsAccessed, values,
                    LocalDateTime.now());

            logger.info("recording and saving log => " + dbLogEntry);
            dbLogEntryRepository.save(dbLogEntry);

            //get table and ids accessed
            allLogTableColumns.addAll(dbLogEntry.getTablesColumnsAccessed());
            allLogIDAccessed.addAll(dbLogEntry.getIdsAccessed());


            logger.info("TableColumns =>"+ allLogTableColumns);

            logger.info("allLogID =>"+ allLogIDAccessed);


            // 4.for each item in the result list check if it causes potential inference
            // attack
            for (PatientInfo pi : resultList) {

                // set inference to false
                pi.setInference(false);
                logger.info("PatientInfo =>" + pi);
                for (Policy p : policies) {

                    columnValuesMap = new HashMap<>();

                    // 5. Get the policyInputColumns
                    List<String> policyInputColumns = new ArrayList<>(p.getInputColumns());

                    logger.info("policyInputColumns =>" + policyInputColumns);

                    // 6. Parse the logical relationship of the inputColumns
                    ResponseObject relationshipData = p.getRelationshipData();
                    ArrayList<String> policyRelationshipOperands = relationshipData.getOperands();
                    logger.info("policyRelationshipOperands=>" + policyRelationshipOperands);

                    // ArrayList<String> policyRelationshipOperands = p.getRelationshipOperands();
                    // Queue<String> policyRelationshipOperators = p.getRelationshipOperators();

                    Queue<String> policyRelationshipOperators = relationshipData.getOperators();
                    logger.info("policyRelationshipOperators=>" + policyRelationshipOperators);

                    // 7. Check if one of the inputColumns is part of the item in focus of the
                    // result list
                    // if so, add the value of that column into the logical relationship
                    for (String operand : policyRelationshipOperands) {
                        String col = operand.split("\\.")[1].trim();
                        String table = operand.split("\\.")[0].trim();
                        if (!table.equals(pi.getTableName())) {
                            if (columnValuesMap.get(table) == null) columnValuesMap.put(operand, new ArrayList<>());
                        } else
                            policyRelationshipOperands.set(policyRelationshipOperands.indexOf(operand), pi.getColumnValue(col));
                    }

                    logger.info("ColumnValues =>"+ columnValuesMap.keySet());

                    //no foreign table policies
                    if(columnValuesMap.keySet().size() == 0){
                        return resultList;
                    }

                    // 9. iterate through each log
                    for (String column : columnValuesMap.keySet()) {
                        if (allLogTableColumns.contains(column)) {
                            for (String id : allLogIDAccessed) {
                                String result = queryRepositories(column, id);
                                logger.info("Result => " + result);
                                columnValuesMap.get(column).add(result);
                                logger.info("tableColumn: " + columnValuesMap.get(column));
                            }
                        }

                    }

                    boolean loop = true;
                    while (loop) {
                        List<String> operands = new ArrayList<String>(policyRelationshipOperands);
                        Queue<String> operators = new LinkedList<String>(policyRelationshipOperators);
                        for (String column : columnValuesMap.keySet()) {
                            if (columnValuesMap.get(column).isEmpty()) {
                                loop = false;
                                break;
                            }
                            logger.info("operands column " + column);
                            operands.set(operands.indexOf(column), columnValuesMap.get(column).remove(0));

                        }

                        // using the operands and operators for this row, check if there is an inference
                        // detection
                        if (isInference(operators, operands)) {
                            pi.setInference(true);

                            // Inference Prevention
                            Collection<? extends GrantedAuthority> roles = getUserRole();

                            boolean isAuthorizedToView = false;
                            for (GrantedAuthority s : roles) {
                                if (s.toString().equals("ROLE_ADMIN")) {
                                    isAuthorizedToView = true;
                                }
                            }

                            // Only block if not admin or doctor
                            if (!isAuthorizedToView) {
                                // Find the blocked columns and change those values if they match
                                List<String> policyBlockedColumns = new ArrayList<>(p.getBlockedColumns());
                                for (String blockedColumn : policyBlockedColumns) {

                                    if (blockedColumn.contains("patient_info")) {

                                        String column = blockedColumn.split("\\.")[1];
                                        if (pi.getColumnValue(column) != null) {
                                            pi.setByColumn(column, "Not Authorized");
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

    public List<PatientMedicalInfo> checkInferenceForPatientMedicalInfo(List<PatientMedicalInfo> resultList,
                                                                        List<String> tablesAndColumnsAccessed)  {

        if (!resultList.isEmpty()) {
            logger.info("results => " + resultList);
            logger.info("table columns accessed => " + tablesAndColumnsAccessed);
            // 1 - First step for the inference detection is to fetch the user
            String currentUserName = getUser();
            logger.info("getUser() => " + currentUserName);

            // 2. get policies related to the query

            List<Policy> policies = policyRepository.findAll();

            logger.info("get policies, found [" + policies.size() + "] policies => " + policies);

            // 3. record a log of the query performed
            List<String> values = resultList.stream().map(e -> String.valueOf(e.getId()))
                    .collect(Collectors.toList());
            logger.info("values => " + values);
            DBLogEntry dbLogEntry = new DBLogEntry(null, currentUserName, tablesAndColumnsAccessed, values,
                    LocalDateTime.now());

            logger.info("recording and saving log => " + dbLogEntry);
            dbLogEntryRepository.save(dbLogEntry);

            //get table and ids accessed
            allLogTableColumns.addAll(dbLogEntry.getTablesColumnsAccessed());
            allLogIDAccessed.addAll(dbLogEntry.getIdsAccessed());

            logger.info("TableColumns =>"+ allLogTableColumns);

            logger.info("allLogID =>"+ allLogIDAccessed);

            // 4.for each item in the result list check if it causes potential inference
            // attack
            for (PatientMedicalInfo pi : resultList) {

                // set inference to false
                pi.setInference(false);
                logger.info("PatientMedicalInfo =>" + pi);
                for (Policy p : policies) {

                    columnValuesMap = new HashMap<>();

                    // 5. Get the policyInputColumns
                    List<String> policyInputColumns = new ArrayList<>(p.getInputColumns());

                    logger.info("policyInputColumns =>" + policyInputColumns);

                    // 6. Parse the logical relationship of the inputColumns
                    ResponseObject relationshipData = p.getRelationshipData();
                    ArrayList<String> policyRelationshipOperands = relationshipData.getOperands();
                    logger.info("policyRelationshipOperands=>" + policyRelationshipOperands);

                    // ArrayList<String> policyRelationshipOperands = p.getRelationshipOperands();
                    // Queue<String> policyRelationshipOperators = p.getRelationshipOperators();

                    Queue<String> policyRelationshipOperators = relationshipData.getOperators();
                    logger.info("policyRelationshipOperators=>" + policyRelationshipOperators);

                    // 7. Check if one of the inputColumns is part of the item in focus of the
                    // result list
                    // if so, add the value of that column into the logical relationship
                    for (String operand : policyRelationshipOperands) {
                        String col = operand.split("\\.")[1].trim();
                        String table = operand.split("\\.")[0].trim();
                        if (!table.equals(pi.getTableName())) {
                            if (columnValuesMap.get(table) == null) columnValuesMap.put(operand, new ArrayList<>());
                        } else
                            policyRelationshipOperands.set(policyRelationshipOperands.indexOf(operand), pi.getColumnValue(col));
                    }


                    logger.info("ColumnValues =>"+ columnValuesMap.keySet());

                    //no foreign table policies
                    if(columnValuesMap.keySet().size() == 0){
                        return resultList;
                    }


                    // 9. iterate through each log
                    for (String column : columnValuesMap.keySet()) {
                        if (allLogTableColumns.contains(column)) {
                            for (String id : allLogIDAccessed) {
                                String result = queryRepositories(column, id);
                                logger.info("Result => " + result);
                                columnValuesMap.get(column).add(result);
                                logger.info("tableColumn: " + columnValuesMap.get(column));
                            }
                        }

                    }

                    boolean loop = true;
                    while (loop) {
                        List<String> operands = new ArrayList<String>(policyRelationshipOperands);
                        Queue<String> operators = new LinkedList<String>(policyRelationshipOperators);
                        for (String column : columnValuesMap.keySet()) {
                            if (columnValuesMap.get(column).isEmpty()) {
                                loop = false;
                                break;
                            }
                            operands.set(operands.indexOf(column), columnValuesMap.get(column).remove(0));

                        }

                        // using the operands and operators for this row, check if there is an inference
                        // detection
                        if (isInference(operators, operands)) {
                            pi.setInference(true);

                            // Inference Prevention
                            Collection<? extends GrantedAuthority> roles = getUserRole();

                            boolean isAuthorizedToView = false;
                            for (GrantedAuthority s : roles) {
                                if (s.toString().equals("ROLE_ADMIN")) {
                                    isAuthorizedToView = true;
                                }
                            }

                            // Only block if not admin or doctor
                            if (!isAuthorizedToView) {
                                // Find the blocked columns and change those values if they match
                                List<String> policyBlockedColumns = new ArrayList<>(p.getBlockedColumns());
                                for (String blockedColumn : policyBlockedColumns) {
                                    if (blockedColumn.contains("patient_medical_info")) {
                                        String column = blockedColumn.split("\\.")[1];
                                        if (pi.getColumnValue(column) != null) {
                                            pi.setByColumn(column, "Not Authorized");
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


    public List<BillingInfo> checkInferenceForBillingInfo(List<BillingInfo> resultList,
                                                                 List<String> tablesAndColumnsAccessed) {

        if (!resultList.isEmpty()) {
            logger.info("results => " + resultList);
            logger.info("table columns accessed => " + tablesAndColumnsAccessed);
            // 1 - First step for the inference detection is to fetch the user
            String currentUserName = getUser();
            logger.info("getUser() => " + currentUserName);

            // 2. get policies related to the query

            List<Policy> policies = policyRepository.findAll();

            logger.info("get policies, found [" + policies.size() + "] policies => " + policies);

            // 3. record a log of the query performed
            List<String> values = resultList.stream().map(e -> String.valueOf(e.getId()))
                    .collect(Collectors.toList());
            logger.info("values => " + values);
            DBLogEntry dbLogEntry = new DBLogEntry(null, currentUserName, tablesAndColumnsAccessed, values,
                    LocalDateTime.now());

            logger.info("recording and saving log => " + dbLogEntry);
            dbLogEntryRepository.save(dbLogEntry);

            //get table and ids accessed
            allLogTableColumns.addAll(dbLogEntry.getTablesColumnsAccessed());
            allLogIDAccessed.addAll(dbLogEntry.getIdsAccessed());

            logger.info("TableColumns =>"+ allLogTableColumns);

            logger.info("allLogID =>"+ allLogIDAccessed);

            // 4.for each item in the result list check if it causes potential inference
            // attack
            for (BillingInfo pi : resultList) {

                // set inference to false
                pi.setInference(false);
                logger.info("Billing Info =>" + pi);
                for (Policy p : policies) {

                    columnValuesMap = new HashMap<>();

                    // 5. Get the policyInputColumns
                    List<String> policyInputColumns = new ArrayList<>(p.getInputColumns());

                    logger.info("policyInputColumns =>" + policyInputColumns);

                    // 6. Parse the logical relationship of the inputColumns
                    ResponseObject relationshipData = p.getRelationshipData();
                    ArrayList<String> policyRelationshipOperands = relationshipData.getOperands();
                    logger.info("policyRelationshipOperands=>" + policyRelationshipOperands);

                    // ArrayList<String> policyRelationshipOperands = p.getRelationshipOperands();
                    // Queue<String> policyRelationshipOperators = p.getRelationshipOperators();

                    Queue<String> policyRelationshipOperators = relationshipData.getOperators();
                    logger.info("policyRelationshipOperators=>" + policyRelationshipOperators);

                    // 7. Check if one of the inputColumns is part of the item in focus of the
                    // result list
                    // if so, add the value of that column into the logical relationship
                    for (String operand : policyRelationshipOperands) {
                        String col = operand.split("\\.")[1].trim();
                        String table = operand.split("\\.")[0].trim();
                        if (!table.equals(pi.getTableName())) {
                            if (columnValuesMap.get(table) == null) columnValuesMap.put(operand, new ArrayList<>());
                        } else
                            policyRelationshipOperands.set(policyRelationshipOperands.indexOf(operand), pi.getColumnValue(col));
                    }


                    logger.info("ColumnValues =>"+ columnValuesMap.keySet());

                    //no foreign table policies
                    if(columnValuesMap.keySet().size() == 0){
                        return resultList;
                    }


                    // 9. iterate through each log
                    for (String column : columnValuesMap.keySet()) {
                        if (allLogTableColumns.contains(column)) {
                            for (String id : allLogIDAccessed) {
                                String result = queryRepositories(column, id);
                                logger.info("Result => " + result);
                                columnValuesMap.get(column).add(result);
                                logger.info("tableColumn: " + columnValuesMap.get(column));
                            }
                        }

                    }

                    boolean loop = true;
                    while (loop) {
                        List<String> operands = new ArrayList<String>(policyRelationshipOperands);
                        Queue<String> operators = new LinkedList<String>(policyRelationshipOperators);
                        for (String column : columnValuesMap.keySet()) {
                            if (columnValuesMap.get(column).isEmpty()) {
                                loop = false;
                                break;
                            }
                            operands.set(operands.indexOf(column), columnValuesMap.get(column).remove(0));

                        }

                        // using the operands and operators for this row, check if there is an inference
                        // detection

                        logger.info("BEFORE IS-INFERENCE; operands=>"+operands);

                        if (isInference(operators, operands)) {
                            pi.setInference(true);

                            // Inference Prevention
                            Collection<? extends GrantedAuthority> roles = getUserRole();

                            boolean isAuthorizedToView = false;
                            for (GrantedAuthority s : roles) {
                                if (s.toString().equals("ROLE_ADMIN")) {
                                    isAuthorizedToView = true;
                                }
                            }

                            // Only block if not admin or doctor
                            if (!isAuthorizedToView) {
                                // Find the blocked columns and change those values if they match
                                List<String> policyBlockedColumns = new ArrayList<>(p.getBlockedColumns());
                                for (String blockedColumn : policyBlockedColumns) {
                                    if (blockedColumn.contains("billing_info")) {
                                        String column = blockedColumn.split("\\.")[1];
                                        if (pi.getColumnValue(column) != null) {
                                            pi.setByColumn(column, "Not Authorized");
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


    // We get the user if they are logged in. If they are not logged in we return
    // null.
    private String getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        } else
            return null;
    }

    private Collection<? extends GrantedAuthority> getUserRole() {
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            logger.info("in get user role" + authentication.getAuthorities());
            return authentication.getAuthorities();
        }
        else return null;
    }
    
    private String queryRepositories(String tableColumn, String id){
        logger.info("id queried:"+id+ " for column: "+tableColumn);
        String table = tableColumn.split("\\.")[0];
        String col = tableColumn.split("\\.")[1];

        try {

            switch (table) {

                case "patient_info":

                    switch (col) {
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

                    switch (col) {
                        case "patient_id":
                            return patientMedicallnfoRepository.findById(Long.valueOf(id)).get().getPatientId().toString();
                        case "length_of_stay":
                            return patientMedicallnfoRepository.findById(Long.valueOf(id)).get().getLengthOfStay();
                        case "reason_of_visit":
                            return patientMedicallnfoRepository.findById(Long.valueOf(id)).get().getReasonOfVisit();
                        case "daily_medical_cost":
                            return patientMedicallnfoRepository.findById(Long.valueOf(id)).get().getDailyMedicalCost().toString();
                        default:
                            return null;
                    }

                case "billing_info":

                    switch (col) {
                        case "account_number":
                            return billingInfoRepository.findByAccountNumber(id).getAccountNumber();
                        case "patient_address":
                            return billingInfoRepository.findByAccountNumber(id).getPatientAddress();
                        case "total_medical_costs":
                            return billingInfoRepository.findByAccountNumber(id).getTotalMedicalCosts().toString();
                        default:
                            return null;
                    }

                default:
                    return null;

            }
        }catch(NumberFormatException|NullPointerException|NoSuchElementException e){

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











