package com.rc.Bank_Service.service;

import com.rc.Bank_Service.dto.DebitCardDTO;
import com.rc.Bank_Service.dto.MpinLoginRequest;
import com.rc.Bank_Service.dto.MpinSetRequest;
import com.rc.Bank_Service.dto.SignUpResponse;
import com.rc.Bank_Service.model.Account;
import com.rc.Bank_Service.model.DebitCard;
import com.rc.Bank_Service.model.User;
import com.rc.Bank_Service.repository.AccountRepository;
import com.rc.Bank_Service.repository.DebitCardRepository;
import com.rc.Bank_Service.repository.UserRepository;
import com.rc.Bank_Service.util.MpinValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class MpinService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final DebitCardRepository debitCardRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public MpinService(UserRepository userRepository, AccountRepository accountRepository,
                       DebitCardRepository debitCardRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.debitCardRepository = debitCardRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public String setMpin(MpinSetRequest request) {
        // 1. Validate Non-Trivial Rules (no 123456, 111111, 654321)
        MpinValidator.validateNonTrivialMpin(request.getMpin());

        String identifier = request.getIdentifier().trim();
        User user = findUserByIdentifier(identifier)
                .orElseThrow(() -> new IllegalArgumentException("User not found for identifier: " + identifier));

        // 2. Hash 6-Digit MPIN using BCrypt
        String hashedMpin = passwordEncoder.encode(request.getMpin());
        user.setMpinHash(hashedMpin);
        user.setMpinFailedAttempts(0);
        user.setMpinLockedUntil(null);

        userRepository.save(user);
        return "6-Digit MPIN successfully created and secured!";
    }

    @Transactional
    public SignUpResponse loginWithMpin(MpinLoginRequest request) {
        String identifier = request.getIdentifier().trim();
        User user = findUserByIdentifier(identifier)
                .orElseThrow(() -> new IllegalArgumentException("Invalid login credentials. User not found."));

        // 1. Check if user has set an MPIN
        if (user.getMpinHash() == null) {
            throw new IllegalArgumentException("MPIN has not been set for this account yet. Please set your 6-digit MPIN first.");
        }

        // 2. Check 3-Attempt Lockout
        if (user.getMpinLockedUntil() != null && user.getMpinLockedUntil().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Account MPIN access is locked due to 3 consecutive failed attempts. Try again later or reset your MPIN.");
        }

        // 3. Verify BCrypt MPIN Hash
        boolean matches = passwordEncoder.matches(request.getMpin(), user.getMpinHash());
        if (!matches) {
            int attempts = user.getMpinFailedAttempts() + 1;
            user.setMpinFailedAttempts(attempts);

            if (attempts >= 3) {
                user.setMpinLockedUntil(LocalDateTime.now().plusHours(24));
                userRepository.save(user);
                throw new IllegalArgumentException("Account MPIN access locked! You have entered an incorrect MPIN 3 times consecutively.");
            } else {
                userRepository.save(user);
                throw new IllegalArgumentException("Incorrect MPIN! Warning: " + (3 - attempts) + " attempt(s) remaining before account lockout.");
            }
        }

        // Reset failed attempts on success
        user.setMpinFailedAttempts(0);
        user.setMpinLockedUntil(null);
        userRepository.save(user);

        // Retrieve Account & Virtual Debit Card details
        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Account not found for user"));

        Optional<DebitCard> cardOpt = debitCardRepository.findByAccount(account);
        DebitCardDTO cardDTO = cardOpt.map(c -> new DebitCardDTO(
                c.getCardNumber(),
                c.getCardHolderName(),
                c.getExpiryMonth(),
                c.getExpiryYear(),
                c.getCvv(),
                c.getCardStatus(),
                c.getDailyLimit()
        )).orElse(null);

        return new SignUpResponse(
                "MPIN Authentication Successful! Welcome back, " + user.getFullName(),
                user.getId(),
                user.getCifNumber(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                account.getAccountNumber(),
                account.getBranch().getBranchName(),
                account.getIfscCode(),
                account.getAccountType(),
                account.getBalance(),
                account.getUpiVpa(),
                user.getKycStatus(),
                user.getAccountLimit(),
                cardDTO
        );
    }

    private Optional<User> findUserByIdentifier(String identifier) {
        // Try CIF Number
        Optional<User> byCif = userRepository.findByCifNumber(identifier);
        if (byCif.isPresent()) return byCif;

        // Try Email
        Optional<User> byEmail = userRepository.findByEmail(identifier.toLowerCase());
        if (byEmail.isPresent()) return byEmail;

        // Try Account Number
        Optional<Account> byAcc = accountRepository.findByAccountNumber(identifier);
        if (byAcc.isPresent()) return Optional.of(byAcc.get().getUser());

        return Optional.empty();
    }
}
