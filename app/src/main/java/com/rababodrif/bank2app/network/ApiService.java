package com.rababodrif.bank2app.network;

import com.rababodrif.bank2app.models.AccountInfo;
import com.rababodrif.bank2app.models.ActivationRequest;
import com.rababodrif.bank2app.models.HistoryEvent;
import com.rababodrif.bank2app.models.LoginRequest;
import com.rababodrif.bank2app.models.LoginResponse;
import com.rababodrif.bank2app.models.TransactionResult;
import com.rababodrif.bank2app.models.TransferRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    // Endpoints du service d'authentification
    @POST("auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("auth/activate")
    @Headers("Accept: text/plain")
    Call<String> activate(@Body ActivationRequest request);

    // Endpoints du service de comptes
    @GET("api/accounts/client/{clientId}")
    Call<List<AccountInfo>> getAccountsByClient(
            @Path("clientId") Long clientId,
            @Header("Authorization") String token
    );

    // Endpoints du service de transactions
    @POST("api/transactions/transfer")
    Call<TransactionResult> transfer(
            @Body TransferRequest request,
            @Header("Authorization") String token
    );

    // Endpoints du service d'historique
    @GET("api/history")
    Call<List<HistoryEvent>> getHistoryEvents(
            @Query("entityId") String entityId,
            @Query("fromDate") String fromDate,
            @Query("toDate") String toDate,
            @Header("Authorization") String token
    );
}