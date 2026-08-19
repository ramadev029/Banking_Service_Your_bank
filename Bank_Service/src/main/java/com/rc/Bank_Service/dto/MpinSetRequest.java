package com.rc.Bank_Service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class MpinSetRequest {

    @NotBlank(message = "CIF Number or Email is required")
    private String identifier;

    @NotBlank(message = "6-Digit MPIN is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "MPIN must be exactly 6 digits")
    private String mpin;

    public MpinSetRequest() {}

    public MpinSetRequest(String identifier, String mpin) {
        this.identifier = identifier;
        this.mpin = mpin;
    }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getMpin() { return mpin; }
    public void setMpin(String mpin) { this.mpin = mpin; }
}
