package com.example.demo.view;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletResponse;

import com.example.demo.data.model.DBLogEntry;
import com.example.demo.data.model.Policy;
import com.example.demo.data.model.Role;
import com.example.demo.data.model.User;
import com.example.demo.data.repository.DBLogEntryRepository;
import com.example.demo.data.repository.UserRepository;
import com.example.demo.logic.PolicyManager;
import com.example.demo.logic.RoleManager;
import com.example.demo.logic.UserManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IFSAdminController {

    private final static Logger logger = LoggerFactory.getLogger(IFSAdminController.class);

    @Autowired
    private PolicyManager policyManager;
    
    @Autowired
    private UserManager userManager;
    
    @Autowired
    private RoleManager roleManager;

    @Autowired
    private DBLogEntryRepository dbLogEntryRepository;

    @Autowired
    private PasswordEncoder passwordencoder;

    public IFSAdminController() {
    }

    /**
     * Clear the Log File
     * @param m
     */
    @GetMapping("/clearLog")
    public String clearLogs(Model m) {
        dbLogEntryRepository.deleteAll();
        return "redirect:/logs";
    }

    /**
     * GET the Admin page
     * @param m
     */
    @GetMapping("/admin")
    public String adminPage(Model m) {
        logger.info("User accessed /index");
        List<Policy> policies = policyManager.getAllPolicies();
        logger.info("Data returned from DB: " + policies);
        m.addAttribute("policies", policies);
        return "admin";
    }

    /**
     * GET a policy ID to delete
     * @param m
     */
    @GetMapping("/deletePolicy")
    public String deletePolicy(@RequestParam(name="policyId") int policyId , Model m, HttpServletResponse servletResponse) {
        logger.info("User accessed /deletePolicy");
        if (policyId < 0 || policyId > 999) {
            logger.info("Error - Policy ID value error");
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return "redirect:/admin";
        }
        else {
            policyManager.deletePolicyById(policyId);
            return "redirect:/admin";
        }
    }


    /**
     * GET the add policy page
     * @param m
     */
    @GetMapping("/addPolicy")
    public String addPolicy(Model m) {
        logger.info("GET addPolicy");
        m.addAttribute("validationErr", false);
        m.addAttribute("policy", new Policy());
        return "addPolicy";
    }

    /**
     * POST a new policy
     * @param newPolicyForm
     * @param m
     */
    @PostMapping("addPolicy")
    public String addNewPolicy(@ModelAttribute Policy newPolicyForm, Model m, HttpServletResponse servletResponse){
        if (newPolicyForm == null) {
            logger.info("Error - Form is empty");
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            m.addAttribute("validationErr", true);
        }
        else {
            if (newPolicyForm.getInputColumns() == null || newPolicyForm.getInputColumns().contains("NONE") || newPolicyForm.getInputColumns().isEmpty() || newPolicyForm.getInputColumns().contains(null) || newPolicyForm.getInputColumns().contains("")) {
                logger.info("Error - Empty Input Columns");
                servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                m.addAttribute("validationErr", true);
                return "addPolicy";
            }
            Set<String> set = new HashSet<String>();
            for (String each: newPolicyForm.getInputColumns()) {
                if (!set.add(each)){
                    logger.info("Error - Duplicate Input Columns");
                    servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    m.addAttribute("validationErr", true);
                    return "addPolicy";
                } 
            }


            if (newPolicyForm.getBlockedColumns() == null || newPolicyForm.getBlockedColumns().contains("NONE") || newPolicyForm.getBlockedColumns().isEmpty() || newPolicyForm.getBlockedColumns().contains(null) || newPolicyForm.getBlockedColumns().contains("")) {
                logger.info("Error - Empty Blocked Columns");
                servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                m.addAttribute("validationErr", true);
                return "addPolicy";
            }

            Set<String> set2 = new HashSet<String>();
            for (String each: newPolicyForm.getBlockedColumns()) {
                if (!set2.add(each)){
                    logger.info("Error - Duplicate Blocked Columns");
                    servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    m.addAttribute("validationErr", true);
                    return "addPolicy";
                } 
            }

            if (newPolicyForm.getRelationship() == null || newPolicyForm.getRelationship().isEmpty() || newPolicyForm.getRelationship().length() == 0) {
                logger.info("Error - Empty Relationship");
                servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                m.addAttribute("validationErr", true);
                return "addPolicy";
            }

            String[] relationshipSplit = newPolicyForm.getRelationship().split(" ");
            String[] allowedColumns = {"patient_info.name", "patient_info.date_of_entry", "patient_info.date_of_leave", 
                                     "patient_info.gender", "patient_medical_info.patient_id", "patient_medical_info.reason_of_visit",
                                     "patient_medical_info.length_of_stay"};
            for (String s : relationshipSplit) {
                boolean isValidColumn = Arrays.stream(allowedColumns).anyMatch(s::equals);

                //Only 1 operator allowed between columns
                if (s.contains("+") || s.contains("-") || s.contains("*") || s.contains("/") || s.contains ("=")) {
                    if (s.length() > 1 && !s.equals("!=")) {
                        logger.info("Error - Relationship has invalid operators");
                        servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        m.addAttribute("validationErr", true);
                        return "addPolicy";
                    }
                }
                else if (!isValidColumn) {
                    logger.info("Error - Relationship has invalid columns");
                    servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    m.addAttribute("validationErr", true);
                    return "addPolicy";
                }
            }

            //Check if policy already exists
            List<Policy> policies = policyManager.getAllPolicies();
            for(Policy policy : policies) {
                if(policy.getRelationship().equals(newPolicyForm.getRelationship())){
                    if(newPolicyForm.getInputColumns().size() == policy.getInputColumns().size()){
                        boolean sameInputColumns = true;
                        boolean sameBlockedColumns = true;
                        for(String inputCol: newPolicyForm.getInputColumns()) {
                            logger.info("input col: " + inputCol);
                            logger.info("policy input cols: " + policy.getInputColumns());
                            if (!policy.getInputColumns().contains(inputCol)) {
                                //Different
                                logger.info("different input column");
                                sameInputColumns = false;
                                break;
                            }
                        }

                        if (newPolicyForm.getBlockedColumns().size() == policy.getBlockedColumns().size()) {
                            if (sameInputColumns == true) {
                                for(String blockedCol: newPolicyForm.getBlockedColumns()) {
                                    logger.info("blocked col: " + blockedCol);
                                    if (!policy.getBlockedColumns().contains(blockedCol)) {
                                        //Different
                                        logger.info("different blocked column");
                                        sameBlockedColumns = false;
                                        break;
                                    }
                                }
                                if (sameBlockedColumns == true) {
                                    logger.info("Error - Policy already exists");
                                    m.addAttribute("duplicatePolicy", true);
                                    return "addPolicy";
                                }
                            }
                        }
                    }
                }
            }

            logger.info("Add new policy parameters: " + newPolicyForm);
            logger.info("POST: " + newPolicyForm);
            policyManager.savePolicy(newPolicyForm);
            return "redirect:/admin";
        }        

        return "addPolicy";
    }
    
    /**
     * GET the edit policy page
     * @param policyId
     * @param m
     */
    @GetMapping("/editPolicy")
    public String editPolicy(@RequestParam(name="policyId") int policyId , Model m) {
        logger.info("Editing Policy ID: " + policyId);
        Optional<Policy> policy = policyManager.getPolicyById(policyId);
        m.addAttribute("validationErr", false);
        m.addAttribute("policy", policy.get());
        return "editPolicy";
    }

    /**
     * POST a set of modified policies
     * @param policyId
     * @param policyForm
     * @param m
     */
    @PostMapping("/editPolicy")
    public String savePolicy(@RequestParam(name="policyId") int policyId , @ModelAttribute Policy policyForm, Model m, HttpServletResponse servletResponse) {
        if (policyId < 0 || policyId > 999) {
            logger.info("Error - Policy ID value error");
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            m.addAttribute("validationErr", true);
            Optional<Policy> policy = policyManager.getPolicyById(policyId);
            m.addAttribute("policy", policy.get());
            return "editPolicy";
        }
        if (policyForm == null) {
            logger.info("Error - Form is empty");
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            m.addAttribute("validationErr", true);
            Optional<Policy> policy = policyManager.getPolicyById(policyId);
            m.addAttribute("policy", policy.get());
            return "editPolicy";
        }
        else {
            if (policyForm.getInputColumns() == null || policyForm.getInputColumns().contains("NONE") || policyForm.getInputColumns().isEmpty() || policyForm.getInputColumns().contains(null) || policyForm.getInputColumns().contains("")) {
                logger.info("Error - Empty Input Columns");
                servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                m.addAttribute("validationErr", true);
                Optional<Policy> policy = policyManager.getPolicyById(policyId);
                m.addAttribute("policy", policy.get());
                return "editPolicy";
            }

            Set<String> set = new HashSet<String>();
            for (String each: policyForm.getInputColumns()) {
                if (!set.add(each)){
                    logger.info("Error - Duplicate Input Columns");
                    servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    m.addAttribute("validationErr", true);
                    return "editPolicy";
                } 
            }

            if (policyForm.getBlockedColumns() == null || policyForm.getBlockedColumns().contains("NONE") || policyForm.getBlockedColumns().isEmpty() || policyForm.getBlockedColumns().contains(null) || policyForm.getBlockedColumns().contains("")) {
                logger.info("Error - Empty Blocked Columns");
                servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                m.addAttribute("validationErr", true);
                Optional<Policy> policy = policyManager.getPolicyById(policyId);
                m.addAttribute("policy", policy.get());
                return "editPolicy";
            }

            Set<String> set2 = new HashSet<String>();
            for (String each: policyForm.getBlockedColumns()) {
                if (!set2.add(each)){
                    logger.info("Error - Duplicate Blocked Columns");
                    servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    m.addAttribute("validationErr", true);
                    return "editPolicy";
                } 
            }

            if (policyForm.getRelationship() == null || policyForm.getRelationship().isEmpty() || policyForm.getRelationship().length() == 0) {
                logger.info("Error - Empty Relationship");
                servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                m.addAttribute("validationErr", true);
                Optional<Policy> policy = policyManager.getPolicyById(policyId);
                m.addAttribute("policy", policy.get());
                return "editPolicy";
            }

            String[] relationshipSplit = policyForm.getRelationship().split(" ");
            String[] allowedColumns = {"patient_info.name", "patient_info.date_of_entry", "patient_info.date_of_leave", 
                                     "patient_info.gender", "patient_medical_info.patient_id", "patient_medical_info.reason_of_visit",
                                     "patient_medical_info.length_of_stay"};
            for (String s : relationshipSplit) {
                boolean isValidColumn = Arrays.stream(allowedColumns).anyMatch(s::equals);
                //Only 1 operator allowed between columns
                if (s.contains("+") || s.contains("-") || s.contains("*") || s.contains("/") || s.contains ("=")) {
                    if (s.length() > 1 && !s.equals("!=")) {
                        logger.info("Error - Relationship has invalid operators");
                        servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        m.addAttribute("validationErr", true);
                        return "editPolicy";
                    }
                }
                else if (!isValidColumn) {
                    logger.info("Error - Relationship has invalid columns");
                    servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    m.addAttribute("validationErr", true);
                    return "editPolicy";
                }
                
            }

            //Check if policy already exists
            List<Policy> policies = policyManager.getAllPolicies();
            for(Policy policy : policies) {
                if(policy.getRelationship().equals(policyForm.getRelationship())){
                    if(policyForm.getInputColumns().size() == policy.getInputColumns().size()){
                        boolean sameInputColumns = true;
                        boolean sameBlockedColumns = true;
                        for(String inputCol: policyForm.getInputColumns()) {
                            logger.info("input col: " + inputCol);
                            logger.info("policy input cols: " + policy.getInputColumns());
                            if (!policy.getInputColumns().contains(inputCol)) {
                                //Different
                                logger.info("different input column");
                                sameInputColumns = false;
                                break;
                            }
                        }

                        if (policyForm.getBlockedColumns().size() == policy.getBlockedColumns().size()) {
                            if (sameInputColumns == true) {
                                for(String blockedCol: policyForm.getBlockedColumns()) {
                                    logger.info("blocked col: " + blockedCol);
                                    if (!policy.getBlockedColumns().contains(blockedCol)) {
                                        //Different
                                        logger.info("different blocked column");
                                        sameBlockedColumns = false;
                                        break;
                                    }
                                }
                                if (sameBlockedColumns == true) {
                                    logger.info("Error - Policy already exists");
                                    m.addAttribute("duplicatePolicy", true);
                                    return "editPolicy";
                                }
                            }
                        }
                    }
                }
            }
            
            logger.info("Editing Policy ID: " + policyId);
            logger.info("Policy Form: " + policyForm);
            policyManager.savePolicy(policyForm);
            return "redirect:/editPolicy?policyId=" + policyId;
        }
    }        

    /**
     * GET the log page
     */
    @GetMapping("/logs")
    public String logs(Model m) {
        List<DBLogEntry> logs = dbLogEntryRepository.findAll();
        m.addAttribute("logs", logs);
        return "logs";
    }

    /**
     * GET the users page
     * @param m
     */
    @GetMapping("/users")
    public String users(Model m) {
        logger.info("GET users");
        List<User> users = userManager.getAllUsers();
        m.addAttribute("users", users);        
        m.addAttribute("validationErr", false);
        return "users";
    }

        /**
     * GET the add user page
     * @param m
     */
    @GetMapping("/addUser")
    public String addUser(Model m) {
        logger.info("GET addNewUser");
        List<Role> roles = roleManager.getAllRoles();
        m.addAttribute("user", new User());
        m.addAttribute("availableRoles", roles);
        m.addAttribute("validationErr", false);
        return "addUser";
    }

    @PostMapping("/addUser")
    public String addUser(@ModelAttribute User newUserForm, Model m, HttpServletResponse servletResponse) {
        logger.info("User Form: " + newUserForm);
        List<Role> roles = roleManager.getAllRoles();
        List<User> users = userManager.getAllUsers();
        String userName = newUserForm.getUserName();
        String userPassword = newUserForm.getPassword();
        Role userRole = newUserForm.getRole();

        if (newUserForm == null) {
            logger.info("Error - Form is empty");
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            m.addAttribute("validationErr", "Cannot submit empty form");
            m.addAttribute("availableRoles", roles);
            return "addUser";
        }
        else if(userName.isEmpty() || userPassword.isEmpty()) {
            logger.info("Error - User Name or Password is empty");
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            m.addAttribute("validationErr", "User Name or Password is empty");
            m.addAttribute("availableRoles", roles);
            return "addUser";
        }
        else if(userName.trim().length() == 0 || userPassword.trim().length() == 0) {
            logger.info("Error - User Name or Password only contains spaces");
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            m.addAttribute("validationErr", " User Name or Password only contains spaces");
            m.addAttribute("availableRoles", roles);
            return "addUser";
        }
        else if(userRole == null) {
            logger.info("Error - Role is empty");
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            m.addAttribute("validationErr", "Role is empty");
            m.addAttribute("availableRoles", roles);
            return "addUser";
        }
        else if(!roles.contains(userRole)) {
            logger.info("Error - Role does not exists");
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            m.addAttribute("validationErr", "Role does not exists");
            m.addAttribute("availableRoles", roles);
            return "addUser";
        }
        else {
            for (User user : users) {
                if(user.getUserName().equalsIgnoreCase(userName)) {
                    logger.info("Error - Duplicate users");
                    servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    m.addAttribute("validationErr", "Duplicate users");
                    m.addAttribute("availableRoles", roles);
                    return "addUser";
                }
            }
        }
        
        newUserForm.setPassword(passwordencoder.encode(newUserForm.getPassword()));
        userManager.saveUserInfo(newUserForm);
        return "redirect:/users";
    }
    
    /**
     * GET the edit user page
     * @param m
     */

    @GetMapping("/editUser")
    public String editUser(@RequestParam(name="userId") int userId , Model m) {
        logger.info("Editing User ID: " + userId);
        List<Role> roles = roleManager.getAllRoles();
        Optional<User> user = userManager.getUserById(userId);
        m.addAttribute("validationErr", false);
        m.addAttribute("user", user.get());
        m.addAttribute("availableRoles", roles);
        return "editUser";
    }

    @PostMapping("/editUser")
    public String saveUser(@RequestParam(name="userId") int userId, @ModelAttribute User userForm, Model m, HttpServletResponse servletResponse) {
        logger.info("Saving User ID: " + userId);
        logger.info("User Form: " + userForm);
        List<Role> roles = roleManager.getAllRoles();
        List<User> users = userManager.getAllUsers();
        String userName = userForm.getUserName();
        String userPassword = userForm.getPassword();
        Role userRole = userForm.getRole();

        if (userForm == null) {
            logger.info("Error - Form is empty");
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            m.addAttribute("validationErr", "Form is empty");
            m.addAttribute("availableRoles", roles);
            return "editUser";
        }
        else if(userName.isEmpty() || userPassword.isEmpty()) {
            logger.info("Error - User Name or Password is empty");
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            m.addAttribute("validationErr", "User Name or Password is empty");
            m.addAttribute("availableRoles", roles);
            return "editUser";
        }
        else if(userName.trim().length() == 0 || userPassword.trim().length() == 0) {
            logger.info("Error - User Name or Password only contains spaces");
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            m.addAttribute("validationErr", "User Name or Password only contains spaces");
            m.addAttribute("availableRoles", roles);
            return "editUser";
        }
        else if(!roles.contains(userRole)) {
            logger.info("Error - Role does not exists");
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            m.addAttribute("validationErr", "Role does not exists");
            m.addAttribute("availableRoles", roles);
            return "editUser";
        }
        else {
            for (User user : users) {
                if(user.getUserName().equalsIgnoreCase(userName) && user.getUserId() != userId) {
                    logger.info("Error - Duplicate users");
                    servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    m.addAttribute("validationErr", "Duplicate users");
                    m.addAttribute("availableRoles", roles);
                    return "editUser";
                }
            }
        }
        userForm.setPassword(passwordencoder.encode(userForm.getPassword()));
        userManager.saveUserInfo(userForm);
        return "redirect:/users";
    }

    @GetMapping("/removeUser")
    public String removeUser(@RequestParam(name="userId") int userId , Model m, HttpServletResponse servletResponse) {
        logger.info("Removing User ID: " + userId);
        if (userId < 0 || userId > 999) {
            logger.info("Error - User ID value error");
            servletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            m.addAttribute("validationErr", "User ID value error");
            return "redirect:/users";
        }
        userManager.removeUser(userId);
        return "redirect:/users";
    }
}
