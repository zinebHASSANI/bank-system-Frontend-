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
import com.rababodrif.bank2app.models.ActivationRequest;
import com.rababodrif.bank2app.network.ApiClient;
import com.rababodrif.bank2app.utils.SharedPrefsManager;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivationActivity extends AppCompatActivity {
    private EditText etClientCode, etNewPassword;
    private Button btnActivate;
    private ProgressBar progressBar;
    private SharedPrefsManager prefsManager;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activation);

        initViews();
        prefsManager = new SharedPrefsManager(this);
        username = getIntent().getStringExtra("username");
        Log.d("ACTIVATION", "Username received: " + username);

        btnActivate.setOnClickListener(v -> performActivation());
    }

    private void initViews() {
        etClientCode = findViewById(R.id.etClientCode);
        etNewPassword = findViewById(R.id.etNewPassword);
        btnActivate = findViewById(R.id.btnActivate);
        progressBar = findViewById(R.id.progressBar);
    }


    private void performActivation() {
        String clientCode = etClientCode.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();

        if (clientCode.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Le mot de passe doit contenir au moins 6 caractères", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);

        ActivationRequest request = new ActivationRequest(clientCode, newPassword);
        Call<String> call = ApiClient.getAuthService().activate(request);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    // Afficher un message de succès
                    Toast.makeText(ActivationActivity.this,
                            "Activation réussie! Veuillez vous connecter",
                            Toast.LENGTH_LONG).show();

                    // Redirection vers LoginActivity avec nettoyage de la pile
                    Intent intent = new Intent(ActivationActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        String error = response.errorBody() != null ? response.errorBody().string() : "Erreur inconnue";
                        Toast.makeText(ActivationActivity.this, "Échec d'activation: " + error, Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(ActivationActivity.this, "Erreur technique", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ActivationActivity.this, "Erreur réseau: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("ACTIVATION", "Erreur réseau", t);
            }
        });
    }

    private void setLoading(boolean loading) {
        btnActivate.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}