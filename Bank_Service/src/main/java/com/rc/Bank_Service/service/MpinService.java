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
                .orElseThrow(() -> new IllegalArgumentException("Invalid login credentials. User not found for: " + identifier));

        // 1. Check if user has set an MPIN (or auto-initialize for legacy database accounts)
        if (user.getMpinHash() == null) {
            MpinValidator.validateNonTrivialMpin(request.getMpin());
            user.setMpinHash(passwordEncoder.encode(request.getMpin()));
            userRepository.save(user);
        } else {
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
        }

        // Reset failed attempts on success
        user.setMpinFailedAttempts(0);
        user.setMpinLockedUntil(null);
        userRepository.save(user);

        // Retrieve Account & Virtual Debit Card details
        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new IllegalStateException("Account not found for user: " + user.getCifNumber()));

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
        if (identifier == null || identifier.isBlank()) return Optional.empty();
        String cleanId = identifier.trim();

        // 1. Try CIF Number (both "CIF-10001" and "10001")
        Optional<User> byCif = userRepository.findByCifNumber(cleanId);
        if (byCif.isPresent()) return byCif;
        if (!cleanId.toUpperCase().startsWith("CIF-")) {
            Optional<User> byCifPrefixed = userRepository.findByCifNumber("CIF-" + cleanId);
            if (byCifPrefixed.isPresent()) return byCifPrefixed;
        }

        // 2. Try Email (case-insensitive)
        Optional<User> byEmail = userRepository.findByEmail(cleanId.toLowerCase());
        if (byEmail.isPresent()) return byEmail;

        // 3. Try Phone Number (exact and stripped of +91, spaces, hyphens)
        Optional<User> byPhone = userRepository.findByPhoneNumber(cleanId);
        if (byPhone.isPresent()) return byPhone;

        String digitsOnly = cleanId.replaceAll("[^0-9]", "");
        if (!digitsOnly.isEmpty()) {
            if (digitsOnly.length() == 10) {
                Optional<User> byDigits = userRepository.findByPhoneNumber(digitsOnly);
                if (byDigits.isPresent()) return byDigits;
            } else if (digitsOnly.length() == 12 && digitsOnly.startsWith("91")) {
                Optional<User> byStrip91 = userRepository.findByPhoneNumber(digitsOnly.substring(2));
                if (byStrip91.isPresent()) return byStrip91;
            }
        }

        // 4. Try Account Number
        Optional<Account> byAcc = accountRepository.findByAccountNumber(cleanId);
        if (byAcc.isPresent()) return Optional.of(byAcc.get().getUser());

        // 5. Try PAN Number
        Optional<User> byPan = userRepository.findByPanNumber(cleanId.toUpperCase());
        if (byPan.isPresent()) return byPan;

        // 6. Try Aadhaar Number
        if (digitsOnly.length() == 12) {
            Optional<User> byAadhaar = userRepository.findByAadhaarNumber(digitsOnly);
            if (byAadhaar.isPresent()) return byAadhaar;
        }

        return Optional.empty();
    }
}
