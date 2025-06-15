package com.rababodrif.bank2app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.rababodrif.bank2app.R;
import com.rababodrif.bank2app.models.TransactionResult;
import com.rababodrif.bank2app.models.TransferRequest;
import com.rababodrif.bank2app.network.ApiClient;
import com.rababodrif.bank2app.utils.SharedPrefsManager;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransferActivity extends AppCompatActivity {
    private EditText etToRib, etAmount, etDescription;
    private Button btnTransfer;
    private ProgressBar progressBar;
    private SharedPrefsManager prefsManager;
    private String fromRib;
    private Long accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefsManager = new SharedPrefsManager(this);
        fromRib = getIntent().getStringExtra("rib");
        accountId = getIntent().getLongExtra("id", -1); // -1 comme valeur par défaut
        if (accountId == -1) {
            Toast.makeText(this, "ID de compte invalide", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (fromRib == null || accountId == -1) {
            Toast.makeText(this, "Informations de compte manquantes", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
    }

    private void initViews() {
        etToRib = findViewById(R.id.etToRib);
        etAmount = findViewById(R.id.etAmount);
        etDescription = findViewById(R.id.etDescription);
        btnTransfer = findViewById(R.id.btnTransfer);
        progressBar = findViewById(R.id.progressBar);

        btnTransfer.setOnClickListener(v -> performTransfer());
    }

    private void performTransfer() {
        String toRib = etToRib.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (toRib.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(this, "RIB et montant sont obligatoires", Toast.LENGTH_SHORT).show();
            return;
        }

        if (toRib.equals(fromRib)) {
            Toast.makeText(this, "Vous ne pouvez pas transférer vers le même compte", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(this, "Le montant doit être positif", Toast.LENGTH_SHORT).show();
                return;
            }

            TransferRequest request = new TransferRequest(fromRib, toRib, amount, description);
            executeTransfer(request);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Montant invalide", Toast.LENGTH_SHORT).show();
        }
    }

    private void executeTransfer(TransferRequest request) {
        String token = "Bearer " + prefsManager.getToken();
        setLoading(true);

        Log.d("TRANSFER", "Envoi du transfert: " + request.toString());

        Call<TransactionResult> call = ApiClient.getTransactionService().transfer(request, token);
        call.enqueue(new Callback<TransactionResult>() {
            @Override
            public void onResponse(Call<TransactionResult> call, Response<TransactionResult> response) {
                setLoading(false);
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        Log.d("TRANSFER_SUCCESS", "Réponse: " + response.body().toString());

                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("transfer_amount", request.getAmount());
                        setResult(RESULT_OK, resultIntent);

                        Toast.makeText(TransferActivity.this,
                                "Transfert réussi: " + response.body().getMessage(),
                                Toast.LENGTH_LONG).show();

                        new Handler(Looper.getMainLooper()).postDelayed(() -> finish(), 1500);
                    } else {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Erreur inconnue";
                        Log.e("TRANSFER_ERROR", "Code: " + response.code() + ", Erreur: " + errorBody);
                        Toast.makeText(TransferActivity.this,
                                "Échec du transfert: " + errorBody,
                                Toast.LENGTH_LONG).show();
                    }
                } catch (IOException e) {
                    Log.e("TRANSFER_ERROR", "Erreur de lecture de la réponse", e);
                    Toast.makeText(TransferActivity.this,
                            "Erreur lors du traitement de la réponse",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<TransactionResult> call, Throwable t) {
                setLoading(false);
                Log.e("TRANSFER_FAILURE", "Erreur réseau", t);
                Toast.makeText(TransferActivity.this,
                        "Erreur réseau: " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        btnTransfer.setEnabled(!loading);
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }
}