package com.rc.Bank_Service.triage.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class TriageDatabaseSeeder implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public TriageDatabaseSeeder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("ALTER TABLE failure_classifications ADD COLUMN IF NOT EXISTS is_benchmark BOOLEAN DEFAULT FALSE;");
            jdbcTemplate.execute("ALTER TABLE failure_classifications ADD COLUMN IF NOT EXISTS suite_name VARCHAR(255);");
            jdbcTemplate.execute("ALTER TABLE failure_classifications ADD COLUMN IF NOT EXISTS root_cause TEXT;");
            jdbcTemplate.execute("ALTER TABLE failure_classifications ADD COLUMN IF NOT EXISTS recommended_action TEXT;");
            jdbcTemplate.execute("ALTER TABLE failure_classifications ADD COLUMN IF NOT EXISTS jira_required BOOLEAN DEFAULT FALSE;");
            jdbcTemplate.execute("ALTER TABLE failure_classifications ADD COLUMN IF NOT EXISTS evidence_json TEXT;");
            jdbcTemplate.execute("ALTER TABLE failure_classifications ADD COLUMN IF NOT EXISTS contradicting_evidence_json TEXT;");
        } catch (Exception e) {
            System.err.println("[TriageDatabaseSeeder] Schema Migration Notice: " + e.getMessage());
        }
        System.out.println("[TriageDatabaseSeeder] Live Jenkins CI/CD Ingestion Engine initialized and ready for automated test report ingestion.");
    }
}
