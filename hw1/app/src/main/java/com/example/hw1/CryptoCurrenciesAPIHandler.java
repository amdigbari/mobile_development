package com.example.hw1;

import java.io.IOException;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;

public abstract class CryptoCurrenciesAPIHandler extends Thread {
    private final OkHttpClient client = new OkHttpClient();

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
                requestCallback(Objects.requireNonNull(response.body()).source());
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    abstract void requestCallback(BufferedSource response) throws IOException;
}
