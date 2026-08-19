package com.rc.Bank_Service.controller;

import com.rc.Bank_Service.dto.*;
import com.rc.Bank_Service.service.AuthService;
import com.rc.Bank_Service.service.GovtKycAdapterService;
import com.rc.Bank_Service.service.MpinService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final GovtKycAdapterService govtKycAdapterService;
    private final MpinService mpinService;

    @Autowired
    public AuthController(AuthService authService, GovtKycAdapterService govtKycAdapterService, MpinService mpinService) {
        this.authService = authService;
        this.govtKycAdapterService = govtKycAdapterService;
        this.mpinService = mpinService;
    }

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request) {
        SignUpResponse response = authService.registerCustomer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/kyc/send-aadhaar-otp")
    public ResponseEntity<AadhaarOtpResponse> sendAadhaarOtp(@Valid @RequestBody AadhaarOtpRequest request) {
        AadhaarOtpResponse response = govtKycAdapterService.triggerAadhaarOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/kyc/verify-aadhaar-otp")
    public ResponseEntity<AadhaarOtpResponse> verifyAadhaarOtp(@RequestBody AadhaarOtpRequest request) {
        AadhaarOtpResponse response = govtKycAdapterService.verifyAadhaarOtp(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mpin/set")
    public ResponseEntity<String> setMpin(@Valid @RequestBody MpinSetRequest request) {
        String response = mpinService.setMpin(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mpin/login")
    public ResponseEntity<SignUpResponse> loginWithMpin(@Valid @RequestBody MpinLoginRequest request) {
        SignUpResponse response = mpinService.loginWithMpin(request);
        return ResponseEntity.ok(response);
    }
}
