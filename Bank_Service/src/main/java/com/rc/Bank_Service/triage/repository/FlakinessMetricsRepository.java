package com.rc.Bank_Service.triage.repository;

import com.rc.Bank_Service.triage.model.FlakinessMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlakinessMetricsRepository extends JpaRepository<FlakinessMetrics, Long> {
    Optional<FlakinessMetrics> findByTestName(String testName);
    List<FlakinessMetrics> findByIsQuarantinedTrue();
    List<FlakinessMetrics> findTop10ByOrderByFlakinessScoreDesc();
}
