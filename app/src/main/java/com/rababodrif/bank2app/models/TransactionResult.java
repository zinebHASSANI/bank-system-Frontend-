package com.rababodrif.bank2app.models;

import java.time.LocalDateTime;

public class TransactionResult {
    private String transactionReference;
    private Long accountId;
    private String rib;
    private Double amount;
    private String status;
    private String timestamp;
    private String message;

    // Getters and setters
    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }
    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }
    public String getRib() { return rib; }
    public void setRib(String rib) { this.rib = rib; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}