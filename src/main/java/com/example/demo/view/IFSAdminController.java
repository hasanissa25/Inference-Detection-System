package com.example.demo.view;

import java.util.List;
import java.util.Optional;

import com.example.demo.data.model.DBLogEntry;
import com.example.demo.data.model.Policy;
import com.example.demo.data.repository.DBLogEntryRepository;
import com.example.demo.logic.PolicyManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class IFSAdminController {

    private final static Logger logger = LoggerFactory.getLogger(IFSAdminController.class);

    private PolicyManager policyManager;

    @Autowired
    private DBLogEntryRepository dbLogEntryRepository;
    
    public IFSAdminController(PolicyManager policyManager) {
        this.policyManager = policyManager;
    }

    @GetMapping("/clearLog")
    public String clearLogs(Model m) {
        dbLogEntryRepository.deleteAll();
        return "redirect:/logs";
    }

    @GetMapping("/admin")
    public String adminPage(Model m) {
        logger.info("User accessed /index");
        List<Policy> policies = policyManager.getAllPolicies();
        logger.info("Data returned from DB: " + policies);
        m.addAttribute("policies", policies);
        return "admin";
    }

    @GetMapping("/addPolicy")
    public String addPolicy(Model m) {
        logger.info("GET addPolicy");
        m.addAttribute("policy", new Policy());
        return "addPolicy";
    }

    @PostMapping("addPolicy")
    public String addNewPolicy(@ModelAttribute Policy newPolicyForm, Model m){
        logger.info("Add new policy parameters: " + newPolicyForm);
        logger.info("POST: " + newPolicyForm);
        policyManager.savePolicy(newPolicyForm);
        return "redirect:/admin";
    }
    
    @GetMapping("/editPolicy")
    public String editPolicy(@RequestParam(name="policyId") int policyId , Model m) {
        logger.info("Editing Policy ID: " + policyId);
        Optional<Policy> policy = policyManager.getPolicyById(policyId);
        m.addAttribute("policy", policy.get());
        return "editPolicy";
    }

    @PostMapping("/editPolicy")
    public String savePolicy(@RequestParam(name="policyId") int policyId , @ModelAttribute Policy policyForm, Model m) {
        logger.info("Editing Policy ID: " + policyId);
        logger.info("Policy Form: " + policyForm);
        policyManager.savePolicy(policyForm);
        // Optional<Policy> policy = policyManager.getPolicyById(policyId);
        // m.addAttribute("policy", policy.get());
        return "redirect:/editPolicy?policyId=" + policyId;
    }

    @GetMapping("/logs")
    public String logs(Model m) {
        List<DBLogEntry> logs = dbLogEntryRepository.findAll();
        m.addAttribute("logs", logs);
        return "logs";
    }
    @GetMapping("/users")
    public String users(Model m) {
        return "users";
    }
    @GetMapping("/editUser")
    public String editUser(Model m) {
        return "editUser";
    }
    @GetMapping("/addUser")
    public String addUser(Model m) {
        return "addUser";
    }
}
