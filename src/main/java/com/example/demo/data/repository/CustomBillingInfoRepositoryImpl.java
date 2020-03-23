package com.example.demo.data.repository;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.example.demo.data.model.BillingInfo;

import org.springframework.util.StringUtils;

public class CustomBillingInfoRepositoryImpl implements CustomBillingInfoRepository {
 
    @PersistenceContext
    private EntityManager entityManager;
 
    @Override
    public List<BillingInfo> customSearch(String accountNumber, String patientAddress, Integer totalMedicalCosts) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<BillingInfo> query = cb.createQuery(BillingInfo.class);
        Root<BillingInfo> BillingInfos = query.from(BillingInfo.class);
 
        Path<String> accountNumberPath = BillingInfos.get("accountNumber");
        Path<String> patientAddressPath = BillingInfos.get("patientAddress");
        Path<Integer> totalMedicalCostsPath = BillingInfos.get("totalMedicalCosts");
 
        List<Predicate> predicates = new ArrayList<>();
        if(!StringUtils.isEmpty(accountNumber))
            predicates.add(cb.equal(accountNumberPath, accountNumber));
        if(!StringUtils.isEmpty(patientAddress))
            predicates.add(cb.equal(patientAddressPath, patientAddress));
        if(totalMedicalCosts != null)
            predicates.add(cb.equal(totalMedicalCostsPath, totalMedicalCosts));

        query.select(BillingInfos)
            .where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
 
        return entityManager.createQuery(query)
            .getResultList();
    }
}