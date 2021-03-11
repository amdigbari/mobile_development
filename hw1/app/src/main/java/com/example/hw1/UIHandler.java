package com.example.hw1;

import android.os.Handler;
import android.os.Looper;

import com.badoo.mobile.util.WeakHandler;

public abstract class UIHandler extends Thread {
    public WeakHandler handler = new WeakHandler(Looper.getMainLooper());


    public UIHandler() {}

    @Override
    public void run() {
        handler.post(() -> callback());
    }

    abstract void callback();
}
