package com.rc.Bank_Service.triage.repository;

import com.rc.Bank_Service.triage.model.TestRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestRunRepository extends JpaRepository<TestRun, Long> {
    List<TestRun> findTop10ByOrderByCreatedAtDesc();
}
