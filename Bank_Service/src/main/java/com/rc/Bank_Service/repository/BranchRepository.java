package com.rc.Bank_Service.repository;

import com.rc.Bank_Service.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BranchRepository extends JpaRepository<Branch, String> {
    Optional<Branch> findByBranchCode(String branchCode);
    Optional<Branch> findByIfscCode(String ifscCode);
}
