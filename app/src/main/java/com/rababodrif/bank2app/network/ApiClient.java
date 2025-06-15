package com.rababodrif.bank2app.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.rababodrif.bank2app.models.HistoryEvent;
import com.rababodrif.bank2app.models.HistoryEventType;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient {
    private static final String AUTH_BASE_URL = "http://10.0.2.2:8082/";
    private static final String ACCOUNT_BASE_URL = "http://10.0.2.2:8081/";
    private static final String TRANSACTION_BASE_URL = "http://10.0.2.2:8083/";
    private static final String HISTORY_BASE_URL = "http://10.0.2.2:8084/";

    private static Retrofit authRetrofit;
    private static Retrofit accountRetrofit;
    private static Retrofit transactionRetrofit;
    private static Retrofit historyRetrofit;

    private static OkHttpClient getClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
    }

    public static Retrofit getAuthClient() {
        if (authRetrofit == null) {
            authRetrofit = new Retrofit.Builder()
                    .baseUrl(AUTH_BASE_URL)
                    .client(getClient())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return authRetrofit;
    }

    public static Retrofit getAccountClient() {
        if (accountRetrofit == null) {
            accountRetrofit = new Retrofit.Builder()
                    .baseUrl(ACCOUNT_BASE_URL)
                    .client(getClient())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return accountRetrofit;
    }

    public static Retrofit getTransactionClient() {
        if (transactionRetrofit == null) {
            transactionRetrofit = new Retrofit.Builder()
                    .baseUrl(TRANSACTION_BASE_URL)
                    .client(getClient())
                    .addConverterFactory(GsonConverterFactory.create(getGson()))
                    .build();
        }
        return transactionRetrofit;
    }

    public static Retrofit getHistoryClient() {
        if (historyRetrofit == null) {
            historyRetrofit = new Retrofit.Builder()
                    .baseUrl(HISTORY_BASE_URL)
                    .client(getClient())
                    .addConverterFactory(GsonConverterFactory.create(getGson()))
                    .build();
        }
        return historyRetrofit;
    }

    public static ApiService getAuthService() {
        return getAuthClient().create(ApiService.class);
    }

    public static ApiService getAccountService() {
        return getAccountClient().create(ApiService.class);
    }

    public static ApiService getTransactionService() {
        return getTransactionClient().create(ApiService.class);
    }

    public static ApiService getHistoryService() {
        return getHistoryClient().create(ApiService.class);
    }

    private static Gson getGson() {
        return new GsonBuilder()
                .registerTypeAdapter(HistoryEvent.class, new HistoryEventDeserializer())
                .create();
    }

    private static class HistoryEventDeserializer implements JsonDeserializer<HistoryEvent> {
        @Override
        public HistoryEvent deserialize(JsonElement json, Type typeOfT,
                                        JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            HistoryEvent event = new HistoryEvent();
            event.setId(jsonObject.get("id").getAsString());
            event.setEventId(jsonObject.get("eventId").getAsString());

            // Handle eventType enum
            String eventTypeStr = jsonObject.get("eventType").getAsString();
            event.setEventType(HistoryEventType.valueOf(eventTypeStr));

            event.setTimestamp(jsonObject.get("timestamp").getAsString());
            event.setServiceName(jsonObject.get("serviceName").getAsString());
            event.setEntityId(jsonObject.get("entityId").getAsString());
            event.setDescription(jsonObject.get("description").getAsString());

            if (jsonObject.has("eventData")) {
                JsonObject eventData = jsonObject.getAsJsonObject("eventData");
                Map<String, Object> eventDataMap = new HashMap<>();
                for (Map.Entry<String, JsonElement> entry : eventData.entrySet()) {
                    eventDataMap.put(entry.getKey(), context.deserialize(entry.getValue(), Object.class));
                }
                event.setEventData(eventDataMap);
            }

            return event;
        }
    }
}