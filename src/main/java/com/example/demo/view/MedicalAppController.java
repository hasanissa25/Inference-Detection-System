package com.example.demo.view;

import java.util.List;

import com.example.demo.data.model.PatientInfo;
import com.example.demo.data.model.PatientMedicalInfo;
import com.example.demo.logic.PatientInfoManager;
import com.example.demo.logic.PatientMedicalInfoManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MedicalAppController {

    private final static Logger logger = LoggerFactory.getLogger(MedicalAppController.class);

    private PatientMedicalInfoManager patientMedicalInfoManager;
    private PatientInfoManager patientInfoManager;

    public MedicalAppController(PatientMedicalInfoManager patientMedicalInfoManager, PatientInfoManager patientInfoManager) {
        this.patientMedicalInfoManager = patientMedicalInfoManager;
        this.patientInfoManager = patientInfoManager;
    }

    @GetMapping("/")
    public String medicalAppHomePage() {
        return "index";
    }

    /**
     * GET the patient medical info page
     * @param m
     */
    @GetMapping("patientMedicalInfo")
    public String pateintMedicalInfo(Model m) {
        m.addAttribute("patientMedicalInfoForm", new PatientMedicalInfo());
        return "patientMedicalInfo";
    }

    /**
     * POST a patient medical info search form
     * @param patientMedicalInfoForm
     * @param m
     */
    @PostMapping("patientMedicalInfo")
    public String pateintMedicalInfoSearchForm(@ModelAttribute PatientMedicalInfo patientMedicalInfoForm, Model m) {
        logger.info("Patient Medical Info Search Form parameters:"+patientMedicalInfoForm); 
        List<PatientMedicalInfo> results = patientMedicalInfoManager.search(patientMedicalInfoForm.getPatientId(), patientMedicalInfoForm.getLengthOfStay(), patientMedicalInfoForm.getReasonOfVisit());
        m.addAttribute("searchResults", results);
        m.addAttribute("patientMedicalInfoForm", patientMedicalInfoForm);
        logger.info("Patient Medical Info form results: " + results);
        return "patientMedicalInfo";
    }

    /**
     * GET the patient info page
     * @param m
     */
    @GetMapping("patientInfo")
    public String patientInfo(Model m) {
        m.addAttribute("patientInfoForm", new PatientInfo());
        return "patientInfo";
    }

    /**
     * POST a patient info search form
     * @param patientInfoForm
     * @param m
     */
    @PostMapping("patientInfo")
    public String pateintInfoSearchForm(@ModelAttribute PatientInfo patientInfoForm, Model m) {
        logger.info("Patient Info Search Form parameters:"+patientInfoForm); 
        List<PatientInfo> results = patientInfoManager.search(patientInfoForm.getName(), patientInfoForm.getDateOfEntry(), patientInfoForm.getDateOfLeave(), patientInfoForm.getGender());
        m.addAttribute("searchResults", results);
        m.addAttribute("patientInfoForm", patientInfoForm);
        logger.info("Patient Info form results: " + results);
        return "patientInfo";
    }
}
