package com.rababodrif.bank2app.models;

public class ActivationRequest {
    private String clientCode;
    private String newPassword;

    public ActivationRequest(String clientCode, String newPassword) {
        this.clientCode = clientCode;
        this.newPassword = newPassword;
    }

    public String getClientCode() { return clientCode; }
    public void setClientCode(String clientCode) { this.clientCode = clientCode; }
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}