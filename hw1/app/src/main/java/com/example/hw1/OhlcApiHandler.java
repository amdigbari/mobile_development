package com.example.hw1;

import java.io.IOException;
import java.util.Objects;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSource;

public abstract class OhlcApiHandler extends Thread {
    private final OkHttpClient client = new OkHttpClient();

    private final String url;

    public OhlcApiHandler(String url) {
        this.url = url;
    }


    @Override
    public void run() {
        Request request = new Request.Builder()
                .url(this.url)
                .addHeader("X-CoinAPI-Key", "932E2F14-F512-4E10-B3AB-D35AAA5E8DC5")
                .build();

        try {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);
                requestCallback(Objects.requireNonNull(response.body()).source());
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


    abstract void requestCallback(BufferedSource response) throws IOException;

}
