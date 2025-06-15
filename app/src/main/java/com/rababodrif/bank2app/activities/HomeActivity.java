package com.rababodrif.bank2app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.jakewharton.threetenabp.AndroidThreeTen;
import com.rababodrif.bank2app.R;
import com.rababodrif.bank2app.models.AccountInfo;
import com.rababodrif.bank2app.models.HistoryEvent;
import com.rababodrif.bank2app.network.ApiClient;
import com.rababodrif.bank2app.utils.SharedPrefsManager;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private TextView tvWelcome, tvBalance, tvRib;
    private String id;
    private ProgressBar progressBar;
    private SharedPrefsManager prefsManager;
    private Button btnHistory, btnTransfer;
    private ListView historyListView;
    private List<HistoryEvent> recentHistory = new ArrayList<>();

    private static final int TRANSFER_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        AndroidThreeTen.init(this);
        prefsManager = new SharedPrefsManager(this);

        Log.d("HOME_ACTIVITY", "onCreate - clientId: " + prefsManager.getClientId());
        Log.d("HOME_ACTIVITY", "onCreate - token: " + prefsManager.getToken());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        initViews();
        loadAccountInfo();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == TRANSFER_REQUEST_CODE) {
            if (resultCode == RESULT_OK && data != null) {
                double amount = data.getDoubleExtra("transfer_amount", 0);
                NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
                String formattedAmount = formatter.format(amount);

                Toast.makeText(this,
                        "Transfert de " + formattedAmount + " effectué avec succès",
                        Toast.LENGTH_LONG).show();

                loadAccountInfo();
                loadRecentHistory();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Transfert annulé", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvBalance = findViewById(R.id.tvBalance);
        tvRib = findViewById(R.id.tvRib);
        progressBar = findViewById(R.id.progressBar);
        btnHistory = findViewById(R.id.btnHistory);
        btnTransfer = findViewById(R.id.btnTransfer);
        historyListView = findViewById(R.id.historyListView);

        String username = prefsManager.getUsername();
        if (username != null) {
            displayWelcomeMessage(username);
        }

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
            intent.putExtra("id", this.id);
            startActivity(intent);
        });

        btnTransfer.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, TransferActivity.class);
            intent.putExtra("id", Long.parseLong(id));
            intent.putExtra("rib", tvRib.getText().toString());
            startActivityForResult(intent, TRANSFER_REQUEST_CODE);
        });
    }


    private void loadRecentHistory() {
        String token = "Bearer " + prefsManager.getToken();
        String accountId = this.id;

        if (accountId == null || token == null) {
            Log.e("HISTORY", "ID ou token manquant");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthAgo = now.minusDays(30);
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        Call<List<HistoryEvent>> call = ApiClient.getHistoryService().getHistoryEvents(
                accountId,
                monthAgo.format(formatter),
                now.format(formatter),
                token
        );

        call.enqueue(new Callback<List<HistoryEvent>>() {
            @Override
            public void onResponse(Call<List<HistoryEvent>> call, Response<List<HistoryEvent>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null && !response.body().isEmpty()) {
                        recentHistory = response.body();
                        displayRecentHistory();
                    } else {
                        Log.d("HISTORY", "Aucune donnée d'historique reçue");
                    }
                } else {
                    Log.e("HISTORY", "Erreur serveur: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<HistoryEvent>> call, Throwable t) {
                Log.e("HISTORY", "Erreur réseau", t);
            }
        });
    }

    private void displayRecentHistory() {
        if (recentHistory == null || recentHistory.isEmpty()) {
            TextView emptyView = findViewById(R.id.emptyView);
            emptyView.setText("Aucune opération récente");
            historyListView.setEmptyView(emptyView);
            return;
        }

        List<String> historyStrings = new ArrayList<>();
        DateTimeFormatter displayFormatter = DateTimeFormatter.ofPattern("dd MMM HH:mm", Locale.FRENCH);

        for (HistoryEvent event : recentHistory) {
            try {

                String formattedDate = event.getTimestampAsDateTime().format(displayFormatter);
                String description = formatEventDescription(event);
                historyStrings.add(formattedDate + " - " + description);
            } catch (Exception e) {
                Log.e("HISTORY_DISPLAY", "Erreur formatage événement", e);

                historyStrings.add(event.getDescription());
            }
        }

        Collections.reverse(historyStrings);

        if (historyStrings.size() > 5) {
            historyStrings = historyStrings.subList(0, 5);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                historyStrings
        );
        historyListView.setAdapter(adapter);
    }

    private String formatEventDescription(HistoryEvent event) {
        String description = event.getDescription();

        try {
            if (event.getEventData() != null && event.getEventData().containsKey("amount")) {
                Object amountObj = event.getEventData().get("amount");
                double amount = 0;

                if (amountObj instanceof Double) {
                    amount = (Double) amountObj;
                } else if (amountObj instanceof Integer) {
                    amount = ((Integer) amountObj).doubleValue();
                }

                NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.FRANCE);
                description = description.replace(String.valueOf(amountObj), nf.format(amount));
            }
        } catch (Exception e) {
            Log.e("HISTORY_FORMAT", "Erreur formatage montant", e);
        }

        return description;
    }

    private void loadAccountInfo() {
        Long clientId = prefsManager.getClientId();
        String token = prefsManager.getToken();

        if (clientId == null || token == null) {
            Toast.makeText(this, "Session expirée - Veuillez vous reconnecter", Toast.LENGTH_SHORT).show();
            logout();
            return;
        }

        setLoading(true);

        Call<List<AccountInfo>> call = ApiClient.getAccountService().getAccountsByClient(clientId, "Bearer " + token);

        call.enqueue(new Callback<List<AccountInfo>>() {
            @Override
            public void onResponse(Call<List<AccountInfo>> call, Response<List<AccountInfo>> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    AccountInfo account = response.body().get(0);
                    HomeActivity.this.id = String.valueOf(account.getId());
                    displayAccountInfo(account);
                    loadRecentHistory();
                } else {
                    Toast.makeText(HomeActivity.this, "Aucun compte trouvé", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<AccountInfo>> call, Throwable t) {
                setLoading(false);
                Toast.makeText(HomeActivity.this, "Erreur de chargement: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayAccountInfo(AccountInfo account) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        String formattedBalance = formatter.format(account.getBalance());
        tvBalance.setText(formattedBalance);
        tvRib.setText(account.getRib());
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            logout();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void displayWelcomeMessage(String username) {
        String welcomeMessage = getString(R.string.welcome, username);
        tvWelcome.setText(welcomeMessage);
    }

    private void logout() {
        prefsManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}