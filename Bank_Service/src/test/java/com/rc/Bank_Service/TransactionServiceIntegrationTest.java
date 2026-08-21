package com.rc.Bank_Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rc.Bank_Service.dto.PaymentRequest;
import com.rc.Bank_Service.dto.SignUpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TransactionServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String customerAAccNumber;
    private String customerBAccNumber;
    private String customerACif;

    @BeforeEach
    void setUp() throws Exception {
        // Register Customer A with unique test credentials
        String suffixA = UUID.randomUUID().toString().substring(0, 5);
        SignUpRequest requestA = new SignUpRequest(
                "Singam Ramcharan",
                "ramcharan." + suffixA + "@example.com",
                "SecurePass123!",
                "9" + String.format("%09d", (int)(Math.random() * 1000000000L)),
                "ABCPE" + String.format("%04d", (int)(Math.random() * 10000)) + "F",
                "347892147890", // Mathematically valid Verhoeff Aadhaar
                LocalDate.of(1995, 5, 15),
                "MALE",
                "Bengaluru HQ",
                "SAVINGS_REGULAR",
                "984021"
        );

        String responseA = mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestA)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        customerAAccNumber = objectMapper.readTree(responseA).get("accountNumber").asText();
        customerACif = objectMapper.readTree(responseA).get("cifNumber").asText();

        // Register Customer B with unique test credentials
        String suffixB = UUID.randomUUID().toString().substring(0, 5);
        SignUpRequest requestB = new SignUpRequest(
                "Rama Krishna",
                "ramakrishna." + suffixB + "@example.com",
                "SecurePass123!",
                "9" + String.format("%09d", (int)(Math.random() * 1000000000L)),
                "XYZPE" + String.format("%04d", (int)(Math.random() * 10000)) + "K",
                "212132324343", // Mathematically valid Verhoeff Aadhaar
                LocalDate.of(1992, 8, 20),
                "MALE",
                "Hyderabad Branch",
                "SAVINGS_REGULAR",
                "654321"
        );

        String responseB = mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestB)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        customerBAccNumber = objectMapper.readTree(responseB).get("accountNumber").asText();
    }

    @Test
    @DisplayName("Should successfully process atomic P2P UPI transfer from Customer A to Customer B with MPIN authorization")
    void testSuccessfulP2PUpiTransfer() throws Exception {
        PaymentRequest transferReq = new PaymentRequest();
        transferReq.setAccountNumber(customerAAccNumber);
        transferReq.setRecipient(customerBAccNumber);
        transferReq.setAmount(new BigDecimal("1500.00"));
        transferReq.setTransferMode("UPI");
        transferReq.setMpin("984021");
        transferReq.setRemarks("Rent Payment");

        mockMvc.perform(post("/api/v1/payments/upi")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").exists())
                .andExpect(jsonPath("$.senderName").value("Singam Ramcharan"))
                .andExpect(jsonPath("$.receiverName").value("Rama Krishna"))
                .andExpect(jsonPath("$.amount").value(1500.00));
    }

    @Test
    @DisplayName("Should reject transaction when invalid MPIN is provided")
    void testInvalidMpinRejection() throws Exception {
        PaymentRequest transferReq = new PaymentRequest();
        transferReq.setAccountNumber(customerAAccNumber);
        transferReq.setRecipient(customerBAccNumber);
        transferReq.setAmount(new BigDecimal("500.00"));
        transferReq.setTransferMode("UPI");
        transferReq.setMpin("000000"); // Wrong MPIN

        mockMvc.perform(post("/api/v1/payments/upi")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid 6-Digit MPIN! Transaction authorization failed."));
    }

    @Test
    @DisplayName("Should reject self-transfer attempt")
    void testSelfTransferRejection() throws Exception {
        PaymentRequest transferReq = new PaymentRequest();
        transferReq.setAccountNumber(customerAAccNumber);
        transferReq.setRecipient(customerAAccNumber); // Self transfer
        transferReq.setAmount(new BigDecimal("200.00"));
        transferReq.setTransferMode("UPI");
        transferReq.setMpin("984021");

        mockMvc.perform(post("/api/v1/payments/upi")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Self-transfer not permitted. Recipient must be a different customer account."));
    }

    @Test
    @DisplayName("Should fetch complete Customer Dashboard with accurate Creditor and Debtor details")
    void testGetCustomerDashboardLedger() throws Exception {
        mockMvc.perform(get("/api/v1/customer/dashboard")
                .param("cifNumber", customerACif))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Singam Ramcharan"))
                .andExpect(jsonPath("$.accountNumber").value(customerAAccNumber))
                .andExpect(jsonPath("$.debitCard.cardNumber").exists());
    }
}
