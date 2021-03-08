package com.example.hw1;

import android.os.Handler;
import android.os.Looper;

public abstract class UIHandler extends Thread {
    public Handler handler = new Handler(Looper.getMainLooper());


    public UIHandler() {}

    @Override
    public void run() {
        handler.post(() -> callback());
    }

    abstract void callback();
}
