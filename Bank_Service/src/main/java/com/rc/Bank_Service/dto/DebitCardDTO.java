package com.rc.Bank_Service.dto;

import java.math.BigDecimal;

public class DebitCardDTO {
    private String cardNumber;
    private String cardHolderName;
    private int expiryMonth;
    private int expiryYear;
    private String cvv;
    private String cardStatus;
    private BigDecimal dailyLimit;

    public DebitCardDTO() {}

    public DebitCardDTO(String cardNumber, String cardHolderName, int expiryMonth, int expiryYear, String cvv, String cardStatus, BigDecimal dailyLimit) {
        this.cardNumber = cardNumber;
        this.cardHolderName = cardHolderName;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cvv = cvv;
        this.cardStatus = cardStatus;
        this.dailyLimit = dailyLimit;
    }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getCardHolderName() { return cardHolderName; }
    public void setCardHolderName(String cardHolderName) { this.cardHolderName = cardHolderName; }

    public int getExpiryMonth() { return expiryMonth; }
    public void setExpiryMonth(int expiryMonth) { this.expiryMonth = expiryMonth; }

    public int getExpiryYear() { return expiryYear; }
    public void setExpiryYear(int expiryYear) { this.expiryYear = expiryYear; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public String getCardStatus() { return cardStatus; }
    public void setCardStatus(String cardStatus) { this.cardStatus = cardStatus; }

    public BigDecimal getDailyLimit() { return dailyLimit; }
    public void setDailyLimit(BigDecimal dailyLimit) { this.dailyLimit = dailyLimit; }
}
