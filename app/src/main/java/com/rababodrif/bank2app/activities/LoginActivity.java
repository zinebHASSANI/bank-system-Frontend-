package com.rababodrif.bank2app.activities;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


import com.rababodrif.bank2app.R;
import com.rababodrif.bank2app.models.LoginRequest;
import com.rababodrif.bank2app.models.LoginResponse;
import com.rababodrif.bank2app.network.ApiClient;
import com.rababodrif.bank2app.utils.SharedPrefsManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private SharedPrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        prefsManager = new SharedPrefsManager(this);

        btnLogin.setOnClickListener(v -> performLogin());
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void performLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        LoginRequest request = new LoginRequest(username, password);
        Call<LoginResponse> call = ApiClient.getAuthService().login(request);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    Log.d("LOGIN", "FirstLogin value: " + loginResponse.isFirstLogin());

                    // Sauvegarde des informations
                    prefsManager.saveToken(loginResponse.getToken());
                    prefsManager.saveUserInfo(username, loginResponse.getClientId());

                    if (loginResponse.isFirstLogin()) {
                        Log.d("LOGIN", "First login detected - Redirect to Activation");
                        Intent intent = new Intent(LoginActivity.this, ActivationActivity.class);
                        intent.putExtra("username", username);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.d("LOGIN", "Regular login - Redirect to Home");
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            else {
                    Log.e("LOGIN", "Error: " + response.code() + " - " + response.message());
                    Toast.makeText(LoginActivity.this, "Identifiants incorrects", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                Log.e("LOGIN", "Failure: ", t);
                Toast.makeText(LoginActivity.this, "Erreur de connexion: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        btnLogin.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}