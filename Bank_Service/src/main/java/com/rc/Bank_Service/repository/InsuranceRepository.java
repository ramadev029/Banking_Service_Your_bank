package com.rc.Bank_Service.repository;

import com.rc.Bank_Service.model.Account;
import com.rc.Bank_Service.model.Insurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InsuranceRepository extends JpaRepository<Insurance, Long> {
    List<Insurance> findByAccountOrderByCreatedAtDesc(Account account);
}
