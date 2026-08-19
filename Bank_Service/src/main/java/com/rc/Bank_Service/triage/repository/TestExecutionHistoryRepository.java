package com.rc.Bank_Service.triage.repository;

import com.rc.Bank_Service.triage.model.TestExecutionHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestExecutionHistoryRepository extends JpaRepository<TestExecutionHistory, Long> {
    List<TestExecutionHistory> findByTestNameOrderByCreatedAtDesc(String testName, Pageable pageable);
}
