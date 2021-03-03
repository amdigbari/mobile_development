package com.example.hw1;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CryptoCurrenciesAPIHandler extends Thread {
    private final OkHttpClient client = new OkHttpClient();
    private final Moshi moshi = new Moshi.Builder().build();
    private final JsonAdapter<CryptoResponse> cryptoResponseJsonAdapter = moshi.adapter(CryptoResponse.class);
    private final String url;

    public CryptoCurrenciesAPIHandler(String url) {
        this.url = url;
    }

    @Override
    public void run() {
        Request request = new Request.Builder()
                .url(this.url)
                .addHeader("X-CMC_PRO_API_KEY", "7f8c104d-b9cb-42a4-a384-4262396cb532")
                .build();

        try {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                CryptoResponse jsonResponse = cryptoResponseJsonAdapter.fromJson(Objects.requireNonNull(response.body()).source());

                assert jsonResponse != null;
                UIHandler.showCryptoCurrencies(jsonResponse.data);

                ItemsListFragment.isLoading.set(false);
                ItemsListFragment.isEnded.set(jsonResponse.data.length == 20);
                ItemsListFragment.pageNumber.set(ItemsListFragment.pageNumber.get() + 1);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    static class CryptoResponse {
        CryptoCurrency[] data;
    }
}
