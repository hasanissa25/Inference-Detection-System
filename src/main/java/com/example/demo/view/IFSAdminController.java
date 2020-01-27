package com.example.demo.view;

import java.util.List;
import java.util.Optional;

import com.example.demo.data.model.DBLogEntry;
import com.example.demo.data.model.Policy;
import com.example.demo.data.model.User;
import com.example.demo.data.repository.DBLogEntryRepository;
import com.example.demo.logic.PolicyManager;
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
    public String deletePolicy(@RequestParam(name="policyId") int policyId , Model m) {
        logger.info("User accessed /deletePolicy");
        policyManager.deletePolicyById(policyId);
        return "redirect:/admin";
    }


    /**
     * GET the add policy page
     * @param m
     */
    @GetMapping("/addPolicy")
    public String addPolicy(Model m) {
        logger.info("GET addPolicy");
        m.addAttribute("policy", new Policy());
        return "addPolicy";
    }

    /**
     * POST a new policy
     * @param newPolicyForm
     * @param m
     */
    @PostMapping("addPolicy")
    public String addNewPolicy(@ModelAttribute Policy newPolicyForm, Model m){
        logger.info("Add new policy parameters: " + newPolicyForm);
        logger.info("POST: " + newPolicyForm);
        policyManager.savePolicy(newPolicyForm);
        return "redirect:/admin";
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
    public String savePolicy(@RequestParam(name="policyId") int policyId , @ModelAttribute Policy policyForm, Model m) {
        logger.info("Editing Policy ID: " + policyId);
        logger.info("Policy Form: " + policyForm);
        policyManager.savePolicy(policyForm);
        // Optional<Policy> policy = policyManager.getPolicyById(policyId);
        // m.addAttribute("policy", policy.get());
        return "redirect:/editPolicy?policyId=" + policyId;
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
        return "users";
    }

    /**
     * GET the edit user page
     * @param m
     */

    @GetMapping("/editUser")
    public String editUser(@RequestParam(name="userId") int userId , Model m) {
        logger.info("Editing User ID: " + userId);
        Optional<User> user = userManager.getUserById(userId);
        m.addAttribute("user", user.get());
        return "editUser";
    }

    @PostMapping("/editUser")
    public String savetUser(@RequestParam(name="userId") int userId, @ModelAttribute User userForm, Model m) {
        logger.info("Saving User ID: " + userId);
        logger.info("User Form: " + userForm);
        userForm.setPassword(passwordencoder.encode(userForm.getPassword()));
        userManager.saveUserInfo(userForm);
        return "redirect:/users";
    }

    /**
     * GET the add user page
     * @param m
     */
    @GetMapping("/addUser")
    public String addUser(Model m) {
        logger.info("GET addNewUser");
        m.addAttribute("user", new User());
        return "addUser";
    }

    @PostMapping("/addUser")
    public String addUser(@ModelAttribute User newUserForm, Model m) {
        logger.info("User Form: " + newUserForm);
        newUserForm.setPassword(passwordencoder.encode(newUserForm.getPassword()));
        userManager.saveUserInfo(newUserForm);
        return "redirect:/users";
    }

    @GetMapping("/removeUser")
    public String removeUser(@RequestParam(name="userId") int userId , Model m) {
        logger.info("Removing User ID: " + userId);
        userManager.removeUser(userId);
        return "redirect:/users";
    }
}
