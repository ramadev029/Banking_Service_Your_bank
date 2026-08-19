package com.rc.Bank_Service.service;

import com.rc.Bank_Service.dto.*;
import com.rc.Bank_Service.model.*;
import com.rc.Bank_Service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerDashboardService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BranchRepository branchRepository;
    private final DebitCardRepository debitCardRepository;
    private final TransactionRepository transactionRepository;
    private final LoanRepository loanRepository;
    private final InsuranceRepository insuranceRepository;

    @Autowired
    public CustomerDashboardService(UserRepository userRepository,
                                    AccountRepository accountRepository,
                                    BranchRepository branchRepository,
                                    DebitCardRepository debitCardRepository,
                                    TransactionRepository transactionRepository,
                                    LoanRepository loanRepository,
                                    InsuranceRepository insuranceRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.branchRepository = branchRepository;
        this.debitCardRepository = debitCardRepository;
        this.transactionRepository = transactionRepository;
        this.loanRepository = loanRepository;
        this.insuranceRepository = insuranceRepository;
    }

    @Transactional(readOnly = true)
    public CustomerDashboardDTO getDashboardData(String identifier) {
        String cleanId = identifier != null ? identifier.trim() : "";
        
        Optional<User> userOpt = userRepository.findByCifNumber(cleanId);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(cleanId);
        }
        
        User user = userOpt.orElseThrow(() -> new IllegalArgumentException("Customer not found with ID: " + identifier));

        Account account = accountRepository.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("Account not found for CIF: " + user.getCifNumber()));

        Branch branch = account.getBranch();

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

        List<TransactionDTO> transactionDTOs = transactionRepository.findByAccountOrderByCreatedAtDesc(account)
                .stream()
                .map(t -> {
                    String senderName = "CREDIT".equals(t.getType()) ? "External Sender" : user.getFullName();
                    String receiverName = "DEBIT".equals(t.getType()) ? "Recipient Account" : user.getFullName();

                    if (t.getReceiverVpa() != null && !t.getReceiverVpa().isBlank()) {
                        Optional<Account> rAcc = accountRepository.findByUpiVpa(t.getReceiverVpa());
                        if (rAcc.isEmpty()) {
                            rAcc = accountRepository.findByAccountNumber(t.getReceiverVpa());
                        }
                        if (rAcc.isPresent()) {
                            receiverName = rAcc.get().getUser().getFullName();
                        }
                    }

                    if (t.getSenderVpa() != null && !t.getSenderVpa().isBlank()) {
                        Optional<Account> sAcc = accountRepository.findByUpiVpa(t.getSenderVpa());
                        if (sAcc.isEmpty()) {
                            sAcc = accountRepository.findByAccountNumber(t.getSenderVpa());
                        }
                        if (sAcc.isPresent()) {
                            senderName = sAcc.get().getUser().getFullName();
                        } else if (t.getSenderVpa().contains("Razorpay")) {
                            senderName = "Razorpay Gateway";
                        }
                    }

                    return new TransactionDTO(
                            t.getTransactionId(),
                            t.getType(),
                            t.getCategory(),
                            t.getAmount(),
                            t.getBalanceAfter(),
                            t.getSenderVpa(),
                            t.getReceiverVpa(),
                            senderName,
                            receiverName,
                            t.getStatus(),
                            t.getRemarks(),
                            t.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());

        List<LoanDTO> loanDTOs = loanRepository.findByAccountOrderByCreatedAtDesc(account)
                .stream()
                .map(l -> new LoanDTO(
                        l.getLoanId(),
                        l.getLoanType(),
                        l.getPrincipalAmount(),
                        l.getInterestRate(),
                        l.getTenureMonths(),
                        l.getMonthlyEmi(),
                        l.getRemainingBalance(),
                        l.getStatus(),
                        l.getCreatedAt()
                ))
                .collect(Collectors.toList());

        List<InsuranceDTO> insuranceDTOs = insuranceRepository.findByAccountOrderByCreatedAtDesc(account)
                .stream()
                .map(i -> new InsuranceDTO(
                        i.getPolicyNumber(),
                        i.getPlanName(),
                        i.getCoverageAmount(),
                        i.getMonthlyPremium(),
                        i.getStatus(),
                        i.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return new CustomerDashboardDTO(
                user.getCifNumber(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                account.getAccountNumber(),
                branch != null ? branch.getBranchName() : "Main Digital Branch",
                account.getIfscCode(),
                account.getAccountType(),
                account.getBalance(),
                account.getUpiVpa(),
                user.getKycStatus(),
                user.getAccountLimit(),
                cardDTO,
                transactionDTOs,
                loanDTOs,
                insuranceDTOs
        );
    }
}
