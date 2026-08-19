package com.rc.Bank_Service.repository;

import com.rc.Bank_Service.model.Account;
import com.rc.Bank_Service.model.DebitCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DebitCardRepository extends JpaRepository<DebitCard, Long> {
    Optional<DebitCard> findByAccount(Account account);
    Optional<DebitCard> findByCardNumber(String cardNumber);
    boolean existsByCardNumber(String cardNumber);
}
