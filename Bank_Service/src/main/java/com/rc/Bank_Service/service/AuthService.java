package com.rc.Bank_Service.service;

import com.rc.Bank_Service.dto.DebitCardDTO;
import com.rc.Bank_Service.dto.SignUpRequest;
import com.rc.Bank_Service.dto.SignUpResponse;
import com.rc.Bank_Service.model.Account;
import com.rc.Bank_Service.model.Branch;
import com.rc.Bank_Service.model.DebitCard;
import com.rc.Bank_Service.model.User;
import com.rc.Bank_Service.repository.AccountRepository;
import com.rc.Bank_Service.repository.BranchRepository;
import com.rc.Bank_Service.repository.DebitCardRepository;
import com.rc.Bank_Service.repository.UserRepository;
import com.rc.Bank_Service.util.AESEncryptionUtil;
import com.rc.Bank_Service.util.VerhoeffAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BranchRepository branchRepository;
    private final DebitCardRepository debitCardRepository;
    private final PasswordEncoder passwordEncoder;
    private final GovtKycAdapterService govtKycAdapterService;

    @Autowired
    public AuthService(UserRepository userRepository, AccountRepository accountRepository,
                       BranchRepository branchRepository, DebitCardRepository debitCardRepository,
                       PasswordEncoder passwordEncoder, GovtKycAdapterService govtKycAdapterService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.branchRepository = branchRepository;
        this.debitCardRepository = debitCardRepository;
        this.passwordEncoder = passwordEncoder;
        this.govtKycAdapterService = govtKycAdapterService;
    }

    @Transactional
    public SignUpResponse registerCustomer(SignUpRequest request) {
        // 1. Bank Risk Check: Legal Age >= 18 Years
        govtKycAdapterService.validateLegalAge(request.getDateOfBirth());

        // 2. Bank Risk Check: PAN 4th Character MUST be 'P' (Individual Person)
        govtKycAdapterService.validatePanEntity(request.getPanNumber());

        // 3. Bank Risk Check: Jaro-Winkler Name Matching (Simulated Tax Record Comparison)
        govtKycAdapterService.validateNameSimilarity(request.getFullName(), request.getFullName());

        // 4. Bank Risk Check: Aadhaar Mathematical Verhoeff Checksum
        String cleanAadhaar = request.getAadhaarNumber().trim();
        if (!VerhoeffAlgorithm.validateAadhaar(cleanAadhaar)) {
            throw new IllegalArgumentException("Invalid Aadhaar Number! Checksum validation failed.");
        }

        // 5. Uniqueness Checks
        String cleanEmail = request.getEmail().trim().toLowerCase();
        String cleanPhone = request.getPhoneNumber().trim();
        String cleanPan = request.getPanNumber().trim().toUpperCase();

        if (userRepository.existsByEmail(cleanEmail)) {
            throw new IllegalArgumentException("An account is already registered with email address: " + cleanEmail);
        }
        if (userRepository.existsByPhoneNumber(cleanPhone)) {
            throw new IllegalArgumentException("An account is already registered with mobile number: " + cleanPhone);
        }
        if (userRepository.existsByPanNumber(cleanPan)) {
            throw new IllegalArgumentException("An account is already registered with PAN Number: " + cleanPan);
        }

        String maskedAadhaar = AESEncryptionUtil.maskAadhaar(cleanAadhaar);
        if (userRepository.existsByAadhaarNumber(maskedAadhaar)) {
            throw new IllegalArgumentException("An account is already registered with Aadhaar Number: " + maskedAadhaar);
        }

        // 6. Ensure Flagship Digital Main Branch Exists (Branch Code: 0001, IFSC: YBRK0000001)
        Branch digitalBranch = branchRepository.findByBranchCode("0001")
                .orElseGet(() -> branchRepository.save(new Branch(
                        "0001",
                        "YourBank Main Digital Branch",
                        "YBRK0000001",
                        "Bengaluru",
                        "Karnataka",
                        "560001"
                )));

        // 7. Generate Sequential Customer CIF ID (CIF-100001)
        long nextSeq = getNextSequenceNumber();
        String cifNumber = "CIF-" + nextSeq;

        // 8. Hash Password & MPIN (if provided) and Save User Entity
        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = new User(
                cifNumber,
                request.getFullName().trim(),
                cleanEmail,
                hashedPassword,
                cleanPhone,
                cleanPan,
                maskedAadhaar,
                request.getDateOfBirth(),
                request.getGender().trim().toUpperCase(),
                request.getAddress().trim()
        );

        if (request.getMpin() != null && !request.getMpin().trim().isEmpty()) {
            com.rc.Bank_Service.util.MpinValidator.validateNonTrivialMpin(request.getMpin().trim());
            newUser.setMpinHash(passwordEncoder.encode(request.getMpin().trim()));
        }

        User savedUser = userRepository.save(newUser);

        // 9. Generate Structured 12-Digit Account Number: 0001 (Branch) + 01 (Savings) + Seq(6)
        String accountNumber = String.format("000101%06d", nextSeq);
        String cleanName = request.getFullName().replaceAll("[^a-zA-Z]", "").toLowerCase();
        String upiVpa = cleanName + (nextSeq % 10000) + "@ybank";

        Account newAccount = new Account(
                savedUser,
                cifNumber,
                accountNumber,
                digitalBranch,
                digitalBranch.getIfscCode(),
                request.getAccountType() != null ? request.getAccountType() : "SAVINGS_REGULAR",
                BigDecimal.ZERO,
                upiVpa
        );

        Account savedAccount = accountRepository.save(newAccount);

        // 10. Mint Instant 16-Digit Virtual Debit Card
        String cardNumber = String.format("4532%012d", nextSeq);
        int expiryMonth = LocalDate.now().getMonthValue();
        int expiryYear = LocalDate.now().getYear() + 5;
        String cvv = String.format("%03d", new SecureRandom().nextInt(1000));

        DebitCard newDebitCard = new DebitCard(
                savedAccount,
                cardNumber,
                savedUser.getFullName(),
                expiryMonth,
                expiryYear,
                cvv
        );

        DebitCard savedCard = debitCardRepository.save(newDebitCard);

        DebitCardDTO debitCardDTO = new DebitCardDTO(
                savedCard.getCardNumber(),
                savedCard.getCardHolderName(),
                savedCard.getExpiryMonth(),
                savedCard.getExpiryYear(),
                savedCard.getCvv(),
                savedCard.getCardStatus(),
                savedCard.getDailyLimit()
        );

        return new SignUpResponse(
                "Welcome to YourBank! Your Account & Virtual Debit Card have been provisioned.",
                savedUser.getId(),
                savedUser.getCifNumber(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber(),
                savedAccount.getAccountNumber(),
                digitalBranch.getBranchName(),
                digitalBranch.getIfscCode(),
                savedAccount.getAccountType(),
                savedAccount.getBalance(),
                savedAccount.getUpiVpa(),
                savedUser.getKycStatus(),
                savedUser.getAccountLimit(),
                debitCardDTO
        );
    }

    private synchronized long getNextSequenceNumber() {
        Optional<User> lastUser = userRepository.findTopByOrderByIdDesc();
        return lastUser.map(user -> 100000L + user.getId() + 1).orElse(100001L);
    }
}
