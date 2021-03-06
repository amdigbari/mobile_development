package com.example.hw1;

import android.os.Handler;
import android.os.Looper;

public class UIHandler extends Thread {
    private CryptoCurrency[] cryptoCurrencies;
    private ItemsListFragment itemsListFragment;

    private CryptoCurrency cryptoCurrencyDetails;

    public UIHandler(CryptoCurrency cryptoCurrencyDetails) {
        this.cryptoCurrencyDetails = cryptoCurrencyDetails;
    }

    public UIHandler(ItemsListFragment itemsListFragment, CryptoCurrency[] cryptoCurrencies) {
        this.cryptoCurrencies = cryptoCurrencies;
        this.itemsListFragment = itemsListFragment;
    }

    @Override
    public void run() {
        if (this.cryptoCurrencies != null) {
            showCryptoCurrencies();
        }
        else if (this.cryptoCurrencyDetails != null) {
            // TODO:
        }
    }

    private void showCryptoCurrencies() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> this.itemsListFragment.setItemsListViewAdaptor(cryptoCurrencies));
    }
}
