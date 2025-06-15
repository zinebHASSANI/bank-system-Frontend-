package com.rababodrif.bank2app.models;
import java.math.BigDecimal;

public class AccountInfo {
    private Long id;
    private BigDecimal balance;
    private String rib;
    private Long clientId;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getRib() { return rib; }
    public void setRib(String rib) { this.rib = rib; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
}