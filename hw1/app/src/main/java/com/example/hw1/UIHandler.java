package com.example.hw1;

import android.os.Handler;
import android.os.Looper;

public class UIHandler {
    public static void showCryptoCurrencies(CryptoCurrency[] cryptoCurrencies) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> showCurrencies(cryptoCurrencies));
    }

    private static void showCurrencies(CryptoCurrency[] cryptoCurrencies) {
        ItemsListFragment.setItemsListViewAdaptor(cryptoCurrencies);
    }
}
