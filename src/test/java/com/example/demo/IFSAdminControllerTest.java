package com.example.demo;

import com.example.demo.data.model.Policy;
import com.example.demo.view.IFSAdminController;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
class IFSAdminControllerTest {
    private IFSAdminController controller;

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMINISTRATOR")
    public void getAdminPage() throws Exception {
        String requestStr = "/admin";

        this.mockMvc.perform(get(requestStr)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Current Database Policies")));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMINISTRATOR")
    public void getAddPolicyPage() throws Exception {
        String requestStr = "/addPolicy";

        this.mockMvc.perform(get(requestStr)).andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Add Input Column")));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMINISTRATOR")
    public void addNewPolicy() throws Exception {
        String requestStr = "/addPolicy?policyId=";
        Policy testPolicy = new Policy();
        List<String> inputCols = new ArrayList<>();
        inputCols.add("patient_info.name");
        List<String> blockedCols = new ArrayList<>();
        blockedCols.add("patient_medical_info.reason_of_visit");
        testPolicy.setInputColumns(inputCols);
        testPolicy.setBlockedColumns(blockedCols);
        testPolicy.setRelationship("patient_info.name");

        this.mockMvc.perform(post(requestStr).content(String.valueOf(testPolicy)))
                .andDo(print())
                .andExpect(content().string(containsString("Add Input Column")));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMINISTRATOR")
    public void addNewPolicyInvalidInputColumns() throws Exception {
        String requestStr = "/addPolicy?policyId=";
        Policy testPolicy = new Policy();
        List<String> inputCols = new ArrayList<>();
        inputCols.add("test");
        List<String> blockedCols = new ArrayList<>();
        blockedCols.add("patient_medical_info.reason_of_visit");
        testPolicy.setInputColumns(inputCols);
        testPolicy.setBlockedColumns(blockedCols);
        testPolicy.setRelationship("patient_info.name");

        this.mockMvc.perform(post(requestStr).content(String.valueOf(testPolicy)))
                .andDo(print())
                .andExpect(content().string(containsString("Server Side Validation Error")));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMINISTRATOR")
    public void addNewPolicyEmptyInputColumns() throws Exception {
        String requestStr = "/addPolicy?policyId=";
        Policy testPolicy = new Policy();
        List<String> inputCols = new ArrayList<>();
        inputCols.add("");
        List<String> blockedCols = new ArrayList<>();
        blockedCols.add("patient_medical_info.reason_of_visit");
        testPolicy.setInputColumns(inputCols);
        testPolicy.setBlockedColumns(blockedCols);
        testPolicy.setRelationship("patient_info.name");

        this.mockMvc.perform(post(requestStr).content(String.valueOf(testPolicy)))
                .andDo(print())
                .andExpect(content().string(containsString("Server Side Validation Error - Empty Input Columns")));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMINISTRATOR")
    public void addNewPolicyInvalidBlockedColumns() throws Exception {
        String requestStr = "/addPolicy?policyId=";
        Policy testPolicy = new Policy();
        List<String> inputCols = new ArrayList<>();
        inputCols.add("patient_info.name");
        List<String> blockedCols = new ArrayList<>();
        blockedCols.add("test");
        testPolicy.setInputColumns(inputCols);
        testPolicy.setBlockedColumns(blockedCols);
        testPolicy.setRelationship("patient_info.name");

        this.mockMvc.perform(post(requestStr).content(String.valueOf(testPolicy)))
                .andDo(print())
                .andExpect(content().string(containsString("Server Side Validation Error")));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMINISTRATOR")
    public void addNewPolicyEmptyBlockedColumns() throws Exception {
        String requestStr = "/addPolicy?policyId=";
        Policy testPolicy = new Policy();
        List<String> inputCols = new ArrayList<>();
        inputCols.add("patient_info.name");
        List<String> blockedCols = new ArrayList<>();
        blockedCols.add("");
        testPolicy.setInputColumns(inputCols);
        testPolicy.setBlockedColumns(blockedCols);
        testPolicy.setRelationship("patient_info.name");

        this.mockMvc.perform(post(requestStr).content(String.valueOf(testPolicy)))
                .andDo(print())
                .andExpect(content().string(containsString("Server Side Validation Error")));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMINISTRATOR")
    public void addNewPolicyInvalidRelationshipColumns() throws Exception {
        String requestStr = "/addPolicy?policyId=";
        Policy testPolicy = new Policy();
        List<String> inputCols = new ArrayList<>();
        inputCols.add("patient_info.name");
        List<String> blockedCols = new ArrayList<>();
        blockedCols.add("patient_medical_info.reason_of_visit");
        testPolicy.setInputColumns(inputCols);
        testPolicy.setBlockedColumns(blockedCols);
        testPolicy.setRelationship("test + patient_info.name");

        this.mockMvc.perform(post(requestStr).content(String.valueOf(testPolicy)))
                .andDo(print())
                .andExpect(content().string(containsString("Server Side Validation Error")));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMINISTRATOR")
    public void addNewPolicyInvalidRelationshipOperators() throws Exception {
        String requestStr = "/addPolicy?policyId=";
        Policy testPolicy = new Policy();
        List<String> inputCols = new ArrayList<>();
        inputCols.add("patient_info.name");
        List<String> blockedCols = new ArrayList<>();
        blockedCols.add("patient_medical_info.reason_of_visit");
        testPolicy.setInputColumns(inputCols);
        testPolicy.setBlockedColumns(blockedCols);
        testPolicy.setRelationship("patient_info.date_of_entry /*+ patient_info.name");

        this.mockMvc.perform(post(requestStr).content(String.valueOf(testPolicy)))
                .andDo(print())
                .andExpect(content().string(containsString("Server Side Validation Error")));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMINISTRATOR")
    public void addNewPolicyEmptyRelationship() throws Exception {
        String requestStr = "/addPolicy?policyId=";
        Policy testPolicy = new Policy();
        List<String> inputCols = new ArrayList<>();
        inputCols.add("patient_info.name");
        List<String> blockedCols = new ArrayList<>();
        blockedCols.add("patient_medical_info.reason_of_visit");
        testPolicy.setInputColumns(inputCols);
        testPolicy.setBlockedColumns(blockedCols);
        testPolicy.setRelationship("");

        this.mockMvc.perform(post(requestStr).content(String.valueOf(testPolicy)))
                .andDo(print())
                .andExpect(content().string(containsString("Server Side Validation Error")));
    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMINISTRATOR")
    public void addNewPolicyDuplicatePolicy() throws Exception {
        //Create one policy first
        String requestStr = "/addPolicy?policyId=";
        Policy testPolicy = new Policy();
        List<String> inputCols = new ArrayList<>();
        inputCols.add("patient_info.name");
        List<String> blockedCols = new ArrayList<>();
        blockedCols.add("patient_medical_info.reason_of_visit");
        testPolicy.setInputColumns(inputCols);
        testPolicy.setBlockedColumns(blockedCols);
        testPolicy.setRelationship("patient_info.name");

        this.mockMvc.perform(post(requestStr).content(String.valueOf(testPolicy)))
                .andDo(print());

        Policy testPolicy2 = new Policy();
        testPolicy2.setInputColumns(inputCols);
        testPolicy2.setBlockedColumns(blockedCols);
        testPolicy2.setRelationship("patient_info.name");

        this.mockMvc.perform(post(requestStr).content(String.valueOf(testPolicy2)))
                .andDo(print())
                .andExpect(content().string(containsString("Server Side Validation Error")));
    }


//    @Test
//    @WithMockUser(username = "admin", password = "admin", roles = "ADMINISTRATOR")
//    public void getEditPolicyPage() throws Exception {
//        //Add a new policy first
//        String requestStr = "/addPolicy?policyId=";
//        Policy testPolicy = new Policy();
//        List<String> inputCols = new ArrayList<>();
//        inputCols.add("patient_info.name");
//        List<String> blockedCols = new ArrayList<>();
//        blockedCols.add("patient_medical_info.reason_of_visit");
//        testPolicy.setInputColumns(inputCols);
//        testPolicy.setBlockedColumns(blockedCols);
//        testPolicy.setRelationship("patient_info.name");
//
//        this.mockMvc.perform(post(requestStr).content(String.valueOf(testPolicy)))
//                .andDo(print());
//
//        //Edit the page for that policy
//        String requestStr2 = "/editPolicy?policyId=1";
//
//        this.mockMvc.perform(get(requestStr2)).andDo(print())
//                .andExpect(status().isOk())
//                .andExpect(content().string(containsString("Edit Policy: ")));
//    }

    @Test
    @WithMockUser(username = "admin", password = "admin", roles = "ADMINISTRATOR")
    public void deletePolicyInvalidID() throws Exception {
         String requestStr = "/deletePolicy?policyId=-1";
         this.mockMvc.perform(get(requestStr)).andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(content().string(containsString("")));
    }

//    @Test
//    @WithMockUser(username = "admin", password = "admin", roles = "ADMINISTRATOR")
//    public void deletePolicyValidID() throws Exception {
//         String requestStr = "/editPolicy?policyId=1";
//         this.mockMvc.perform(get(requestStr)).andDo(print())
//                 .andExpect(status().is3xxRedirection())
//                 .andExpect(content().string(containsString("")));
//    }
}