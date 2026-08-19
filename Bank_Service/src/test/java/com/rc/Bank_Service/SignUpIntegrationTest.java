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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class SignUpIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should successfully register customer")
    void testSignUpSuccess() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "Test User One",
                "test.user.one@example.com",
                "SecureP@ssword123",
                "9876543201",
                "ABCPE1231F",
                "234567890123",
                LocalDate.of(1990, 1, 1),
                "MALE",
                "123 Street, Bengaluru",
                "SAVINGS_REGULAR",
                "984021"
        );

        mockMvc.perform(post("/api/v1/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cifNumber").exists())
                .andExpect(jsonPath("$.accountNumber").exists());
    }
}
