package com.rababodrif.bank2app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {
    private static final String PREF_NAME = "Bank2AppPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_CLIENT_ID = "client_id";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public SharedPrefsManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void saveUserInfo(String username, Long clientId) {
        editor.putString(KEY_USERNAME, username);
        if (clientId != null) {
            editor.putLong(KEY_CLIENT_ID, clientId);
        }
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply();
    }

    public Long getClientId() {
        if (prefs.contains(KEY_CLIENT_ID)) {
            return prefs.getLong(KEY_CLIENT_ID, -1);
        }
        return null;
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }


    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false) && getToken() != null;
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}