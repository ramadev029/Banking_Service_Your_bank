package com.rc.Bank_Service.dto;

public class AadhaarOtpResponse {
    private String txnId;
    private String maskedMobile;
    private String message;
    private boolean verified;
    private String ekycToken;

    public AadhaarOtpResponse() {}

    public AadhaarOtpResponse(String txnId, String maskedMobile, String message, boolean verified, String ekycToken) {
        this.txnId = txnId;
        this.maskedMobile = maskedMobile;
        this.message = message;
        this.verified = verified;
        this.ekycToken = ekycToken;
    }

    public String getTxnId() { return txnId; }
    public void setTxnId(String txnId) { this.txnId = txnId; }

    public String getMaskedMobile() { return maskedMobile; }
    public void setMaskedMobile(String maskedMobile) { this.maskedMobile = maskedMobile; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getEkycToken() { return ekycToken; }
    public void setEkycToken(String ekycToken) { this.ekycToken = ekycToken; }
}
