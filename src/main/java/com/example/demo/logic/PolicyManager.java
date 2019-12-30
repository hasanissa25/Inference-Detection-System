package com.example.demo.logic;

import java.util.List;
import java.util.Optional;

import com.example.demo.data.model.Policy;
import com.example.demo.data.repository.PolicyRepository;

public class PolicyManager {

    private PolicyRepository policyRepository;

    public PolicyManager(PolicyRepository policyRepository) {
        this.policyRepository = policyRepository;
    }

    public void addPolicy(Policy policy) {
        policyRepository.save(policy);
    }

    public List<Policy> getAllPolicies() {
        return policyRepository.findAll();
    }

    public Optional<Policy> getPolicyById(int policyId) {
        return policyRepository.findById((long) policyId);
    }

    public void savePolicy(Policy policyForm) {
        policyRepository.save(policyForm);
    }
}
