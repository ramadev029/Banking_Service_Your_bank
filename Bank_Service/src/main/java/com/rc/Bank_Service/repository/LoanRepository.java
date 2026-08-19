package com.rc.Bank_Service.repository;

import com.rc.Bank_Service.model.Account;
import com.rc.Bank_Service.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByAccountOrderByCreatedAtDesc(Account account);
}
