package com.rababodrif.bank2app.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.rababodrif.bank2app.R;
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

public class HistoryActivity extends AppCompatActivity {
    private ListView listView;
    private SharedPrefsManager prefsManager;
    private List<HistoryEvent> historyEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefsManager = new SharedPrefsManager(this);
        listView = findViewById(R.id.historyListView);

        loadHistory();
    }

    private void loadHistory() {
        String token = "Bearer " + prefsManager.getToken();
        String accountId = getIntent().getStringExtra("id");

        if (accountId == null || token == null) {
            Toast.makeText(this, "Informations de session manquantes", Toast.LENGTH_SHORT).show();
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
                        historyEvents = response.body();
                        displayHistory();
                    } else {
                        showEmptyView("Aucune opération trouvée");
                    }
                } else {
                    try {
                        String error = response.errorBody() != null ?
                                response.errorBody().string() : "Erreur inconnue";
                        Log.e("HISTORY_ERROR", "Erreur serveur: " + error);
                        Toast.makeText(HistoryActivity.this,
                                "Erreur de chargement", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("HISTORY_ERROR", "Erreur lecture réponse", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<HistoryEvent>> call, Throwable t) {
                Log.e("HISTORY_ERROR", "Erreur réseau", t);
                Toast.makeText(HistoryActivity.this,
                        "Erreur réseau: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayHistory() {
        List<String> historyStrings = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.FRENCH);

        for (HistoryEvent event : historyEvents) {
            try {
                String formattedDate = event.getTimestampAsDateTime().format(formatter);
                String description = formatDescription(event);
                historyStrings.add(formattedDate + " - " + description);
            } catch (Exception e) {
                Log.e("HISTORY_FORMAT", "Erreur formatage événement", e);
                historyStrings.add(event.getTimestamp() + " - " + event.getDescription());
            }
        }

        Collections.reverse(historyStrings);

        if (historyStrings.isEmpty()) {
            showEmptyView("Aucune opération à afficher");
            return;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                historyStrings
        );
        listView.setAdapter(adapter);
    }

    private String formatDescription(HistoryEvent event) {
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

    private void showEmptyView(String message) {
        TextView emptyView = findViewById(R.id.emptyView);
        emptyView.setText(message);
        listView.setEmptyView(emptyView);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}