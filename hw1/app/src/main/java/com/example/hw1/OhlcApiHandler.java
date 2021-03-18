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
                .addHeader("X-CoinAPI-Key", "DA0D48B4-6C80-467B-9C3C-B6C75DCA574F")
                .build();

        try {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);
                requestCallback(Objects.requireNonNull(response.body()).source());
            }
        } catch (Exception e1) {
            e1.printStackTrace();
            requestCatchCallback();
        }
    }


    abstract void requestCallback(BufferedSource response) throws IOException;
    abstract void requestCatchCallback();
}
