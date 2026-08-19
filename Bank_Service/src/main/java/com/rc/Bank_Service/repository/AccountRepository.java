package com.rc.Bank_Service.repository;

import com.rc.Bank_Service.model.Account;
import com.rc.Bank_Service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUser(User user);
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByUpiVpa(String upiVpa);
    boolean existsByAccountNumber(String accountNumber);
}
