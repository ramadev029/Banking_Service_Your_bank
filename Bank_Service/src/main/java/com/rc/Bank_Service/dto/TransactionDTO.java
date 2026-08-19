package com.rc.Bank_Service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionDTO {

    private String transactionId;
    private String type; // CREDIT or DEBIT
    private String category;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String senderVpa;
    private String receiverVpa;
    private String senderName;
    private String receiverName;
    private String status;
    private String remarks;
    private LocalDateTime createdAt;

    public TransactionDTO() {}

    public TransactionDTO(String transactionId, String type, String category, BigDecimal amount,
                          BigDecimal balanceAfter, String senderVpa, String receiverVpa, String status,
                          String remarks, LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.senderVpa = senderVpa;
        this.receiverVpa = receiverVpa;
        this.status = status;
        this.remarks = remarks;
        this.createdAt = createdAt;
    }

    public TransactionDTO(String transactionId, String type, String category, BigDecimal amount,
                          BigDecimal balanceAfter, String senderVpa, String receiverVpa,
                          String senderName, String receiverName, String status,
                          String remarks, LocalDateTime createdAt) {
        this.transactionId = transactionId;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.senderVpa = senderVpa;
        this.receiverVpa = receiverVpa;
        this.senderName = senderName;
        this.receiverName = receiverName;
        this.status = status;
        this.remarks = remarks;
        this.createdAt = createdAt;
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public void setBalanceAfter(BigDecimal balanceAfter) { this.balanceAfter = balanceAfter; }

    public String getSenderVpa() { return senderVpa; }
    public void setSenderVpa(String senderVpa) { this.senderVpa = senderVpa; }

    public String getReceiverVpa() { return receiverVpa; }
    public void setReceiverVpa(String receiverVpa) { this.receiverVpa = receiverVpa; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
