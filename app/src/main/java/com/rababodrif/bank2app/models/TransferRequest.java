package com.rababodrif.bank2app.models;

public class TransferRequest {

    private String fromRib;
    private String toRib;
    private double amount;
    private String description;

    public TransferRequest(String fromRib, String toRib, double amount, String description) {

        this.fromRib = fromRib;
        this.toRib = toRib;
        this.amount = amount;
        this.description = description;
    }

    // Getters and setters
    public String getFromRib() { return fromRib; }
    public void setFromRib(String fromRib) { this.fromRib = fromRib; }
    public String getToRib() { return toRib; }
    public void setToRib(String toRib) { this.toRib = toRib; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}