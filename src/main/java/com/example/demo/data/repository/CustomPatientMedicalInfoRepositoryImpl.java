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

import com.example.demo.data.model.PatientMedicalInfo;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class CustomPatientMedicalInfoRepositoryImpl implements CustomPatientMedicalInfoRepository {
 
    @PersistenceContext
    private EntityManager entityManager;
 
    @Override
    public List<PatientMedicalInfo> customSearch(Long patientId, String lengthOfStay, String reasonOfVisit, Integer dailyMedicalCost) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<PatientMedicalInfo> query = cb.createQuery(PatientMedicalInfo.class);
        Root<PatientMedicalInfo> patientMedicalInfos = query.from(PatientMedicalInfo.class);
 
        Path<Long> patientIdPath = patientMedicalInfos.get("patientId");
        Path<String> reasonOfVisitPath = patientMedicalInfos.get("reasonOfVisit");
        Path<String> lengthOfStayPath = patientMedicalInfos.get("lengthOfStay");
        Path<Integer> dailyMedicalCostPath = patientMedicalInfos.get("dailyMedicalCost");
 
        List<Predicate> predicates = new ArrayList<>();
        if(patientId != null)
            predicates.add(cb.equal(patientIdPath, patientId));
        if(!StringUtils.isEmpty(reasonOfVisit))
            predicates.add(cb.equal(reasonOfVisitPath, reasonOfVisit));
        if(!StringUtils.isEmpty(lengthOfStay))
            predicates.add(cb.equal(lengthOfStayPath, lengthOfStay));
        if(!StringUtils.isEmpty(dailyMedicalCost))
            predicates.add(cb.equal(dailyMedicalCostPath, dailyMedicalCost));

        query.select(patientMedicalInfos)
            .where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
 
        return entityManager.createQuery(query)
            .getResultList();
    }
}