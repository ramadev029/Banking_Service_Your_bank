package com.rc.Bank_Service.repository;

import com.rc.Bank_Service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByCifNumber(String cifNumber);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByPanNumber(String panNumber);
    boolean existsByAadhaarNumber(String aadhaarNumber);
    Optional<User> findTopByOrderByIdDesc();
}
