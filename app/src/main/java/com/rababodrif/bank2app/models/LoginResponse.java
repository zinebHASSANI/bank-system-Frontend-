package com.rababodrif.bank2app.models;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("token")
    private String token;

    @SerializedName("firstLogin")
    private boolean firstLogin;

    @SerializedName("message")
    private String message;

    @SerializedName("clientId")
    private Long clientId;

    // Getters et setters
    public String getToken() { return token; }
    public boolean isFirstLogin() { return firstLogin; }
    public String getMessage() { return message; }
    public Long getClientId() { return clientId; }

}