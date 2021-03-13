package com.example.hw1;

import android.os.Looper;

import com.badoo.mobile.util.WeakHandler;

public abstract class UIHandler extends Thread {
    public WeakHandler handler = new WeakHandler(Looper.getMainLooper());


    public UIHandler() {
        super();
    }

    @Override
    public void run() {
        handler.post(this::callback);
    }

    abstract void callback();
}
