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
import com.example.demo.data.repository.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

    @Autowired
    private TableRepository tableRepository;

    public <T extends SuperTable> List<T> checkForInference(Class<T> type, List<T> resultList,
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

            // 4.for each item in the result list check if it causes potential inference

            for (T item : resultList) {
                item = type.cast(item);
                // set inference to false
                item.setInference(false);
                logger.info(type.toString() + "=> "+ item);
                for (Policy p : policies) {
                    // 5. Get the policyInputColumns
                    List<String> policyInputColumns = new ArrayList<>(p.getInputColumns());

                    logger.info("policyInputColumns =>" + policyInputColumns);

                    // 6. Parse the logical relationship of the inputColumns
                    ResponseObject relationshipData = p.getRelationshipData();
                    ArrayList<String> policyRelationshipOperands = relationshipData.getOperands();
                    logger.info("policyRelationshipOperands=>" + policyRelationshipOperands);

                    Queue<String> policyRelationshipOperators = relationshipData.getOperators();
                    logger.info("policyRelationshipOperators=>" + policyRelationshipOperators);

                    //map tables to their columns & fill in values for policyRelationshipOperand
                    Map<String, Set<String>> tablesColumns = new HashMap<>();
                    for(String operand : policyRelationshipOperands){
                        String col = operand.split("\\.")[1].trim();
                        String table = operand.split("\\.")[0].trim();
                        if(!table.equals(item.getTableName())) {
                            if (tablesColumns.get(table) == null) tablesColumns.put(operand, new HashSet<>());
                        }
                        else policyRelationshipOperands.set(policyRelationshipOperands.indexOf(operand), item.getColumnValue(col));
                    }


                    // 7. Get the logs that have accessed the policyInputColumns
                    List<DBLogEntry> logEntries = dbLogEntryRepository
                            .findDistinctByTablesColumnsAccessedIn(policyInputColumns);
                    logger.info("logEntries =>" + logEntries);

                    Set<String> allLogTableColumns = new HashSet<>();
                    Set<String> allLogIDAccessed = new HashSet<>();

                    // 9. iterate through each log
                    for (DBLogEntry entry : logEntries) {
                        logger.info("Log entry=>" + entry);
                        // get table columns and IDS accessed in each log
                        allLogTableColumns.addAll(entry.getTablesColumnsAccessed());
                        allLogIDAccessed.addAll(entry.getIdsAccessed());

                    }
                    for (String key : tablesColumns.keySet()) {
                            if (allLogTableColumns.contains(key)) {
                                for (String id : allLogIDAccessed) {
                                    String result = queryRepositories(key, id);
                                    tablesColumns.get(key).add(result);
                                }
                            }
                    }

                    boolean flag = true;
                    while(flag){
                        List<String> operands = new ArrayList<String>(policyRelationshipOperands);
                        Queue<String> operators = new LinkedList<String>(policyRelationshipOperators);

                        for(Map.Entry<String, Set<String>> entry : tablesColumns.entrySet()) {
                            if(entry.getValue().size() == 0){
                                flag = false;
                                break;
                            }
                            else {
                                operands.set(operands.indexOf(entry.getKey()), new ArrayList<String>(entry.getValue()).get(0));
                                entry.getValue().remove(new ArrayList<String>(entry.getValue()).get(0));
                            }
                        }
                            // using the operands and operators for this row, check if there is an inference
                            // detection
                            if (isInference(operators, operands)) {
                                item.setInference(true);

                                // Inference Prevention
                                Collection<? extends GrantedAuthority> roles = getUserRole();

                                boolean isAuthorizedToView = false;
                                for (GrantedAuthority s : roles){
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
                                            if (item.getColumnValue(column) != null) {
                                                item.setByColumn(column, "Not Authorized");
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

    private String queryRepositories(String tableCol, String id){
        logger.info("id queried:"+id);
        String col = tableCol.split("\\.")[1].trim();
        String table = tableCol.split("\\.")[0].trim();
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
                return patientMedicallnfoRepository.findByPatientID(id).getPatientId().toString();
                case "length_of_stay":
                return patientMedicallnfoRepository.findByPatientID(id).getLengthOfStay();
                case "reason_of_visit":
                return patientMedicallnfoRepository.findByPatientID(id).getReasonOfVisit();
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
            
            LocalDate date1 = LocalDate.parse(operand1, dateFormatter);
            LocalDate date2 = LocalDate.parse(operand2, dateFormatter);
            
            switch(operator.trim()){
                
                case "-":
                    daysBetween = (int) ChronoUnit.DAYS.between(date1, date2);
                    if (daysBetween < 0){
                        daysBetween = (int) ChronoUnit.DAYS.between(date2, date1);
                    }

                    return String.valueOf(daysBetween);
                case "==":
                    if(date1.equals(date2))result = true;
                    else result = false;

                    return Boolean.toString(result);
                
                case "!=":
                    if(!date1.equals(date2))result = true;
                    else result = false;

                    return Boolean.toString(result);
                default:
                    return null;
            }
            
        }
        else if(isInteger(operand1) && isInteger(operand2)){
            
            arg1 = Integer.valueOf(operand1);
            arg2 = Integer.valueOf(operand2);
            switch(operator.trim()) {
                
                case "-":
                    return String.valueOf(arg1 - arg2);
                case "+":
                    return String.valueOf(arg1 + arg2);
                case "/":
                    return String.valueOf(arg1 / arg2);
                case "*":
                    return String.valueOf(arg1 * arg2);
                case "==":
                    if(arg1 != arg2)result = true;
                    else result = false;
                    return Boolean.toString(result);
                case "!=":
                    if(arg1 == arg2)result = true;
                    else result = false;
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





