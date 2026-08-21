package com.rc.Bank_Service.triage.repository;

import com.rc.Bank_Service.triage.model.FailureClassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FailureClassificationRepository extends JpaRepository<FailureClassification, Long> {
    List<FailureClassification> findByIsHumanApprovedFalseOrderByCreatedAtDesc();
    List<FailureClassification> findByCategoryAndIsHumanApprovedFalseOrderByCreatedAtDesc(String category);
    List<FailureClassification> findTop20ByOrderByCreatedAtDesc();
}
