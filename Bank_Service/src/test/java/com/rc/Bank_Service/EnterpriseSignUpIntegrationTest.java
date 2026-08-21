package com.rc.Bank_Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rc.Bank_Service.dto.SignUpRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EnterpriseSignUpIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should successfully register a new customer with CIF, structured account, MPIN, and virtual debit card")
    void testSuccessfulEnterpriseSignUp() throws Exception {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 5);
        SignUpRequest request = new SignUpRequest(
                "Kanna Kumar",
                "kanna.kumar." + randomSuffix + "@example.com",
                "SecureP@ssword123",
                "9" + String.format("%09d", (int)(Math.random() * 1000000000L)),
                "ABCPE" + String.format("%04d", (int)(Math.random() * 10000)) + "F",
                "212132324343", // Mathematically valid Verhoeff Aadhaar
                LocalDate.of(1995, 5, 15),
                "MALE",
                "45, MG Road, Bengaluru, Karnataka",
                "SAVINGS_REGULAR",
                "984021"
        );

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cifNumber").exists())
                .andExpect(jsonPath("$.accountNumber").exists())
                .andExpect(jsonPath("$.debitCard.cardNumber").exists());
    }

    @Test
    @DisplayName("Should reject sign-up when Aadhaar fails Verhoeff algorithm checksum")
    void testInvalidAadhaarVerhoeffChecksum() throws Exception {
        String randomSuffix = UUID.randomUUID().toString().substring(0, 5);
        SignUpRequest request = new SignUpRequest(
                "Fake User",
                "fake.user." + randomSuffix + "@example.com",
                "SecureP@ssword123",
                "9" + String.format("%09d", (int)(Math.random() * 1000000000L)),
                "XYZPE" + String.format("%04d", (int)(Math.random() * 10000)) + "K",
                "234567890123", // Passes @Pattern(2-9 + 11 digits) but FAILS Verhoeff checksum
                LocalDate.of(1998, 1, 1),
                "MALE",
                "Bengaluru",
                "SAVINGS_REGULAR",
                "984021"
        );

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Invalid Aadhaar Number! Checksum validation failed."));
    }
}
