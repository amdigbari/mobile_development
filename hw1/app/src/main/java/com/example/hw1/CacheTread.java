package com.example.hw1;

import android.annotation.SuppressLint;
import android.content.Context;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

import static com.google.android.material.internal.ContextUtils.getActivity;

public abstract class CacheTread extends Thread {
    private final boolean isWrite;
    private CryptoCurrency[] cryptoCurrencies;
    private final Context context;
    private final String filename = "crypto_currencies.json";
    private final Moshi moshi = new Moshi.Builder().build();
    private final JsonAdapter<CryptoCurrency[]> cryptoResponseJsonAdapter = moshi.adapter((Type) CryptoCurrency.class);

    public CacheTread(Context context, CryptoCurrency[] cryptoCurrencies) {
        this.context = context;
        this.isWrite = true;
        this.cryptoCurrencies = cryptoCurrencies;
    }

    public CacheTread(Context context) {
        this.context = context;
        this.isWrite = false;
    }

    @Override
    public void run() {
        if (this.isWrite) {
            writeToFile();
        } else {
            readFromFile();
        }
    }

    private void writeToFile() {
        try {
            @SuppressLint("RestrictedApi") ObjectOutput out = new ObjectOutputStream(new FileOutputStream
                    (new File(Objects.requireNonNull(getActivity(this.context)).getCacheDir(), "") + File.separator + this.filename));
            out.writeObject(Arrays.toString(cryptoCurrencies));
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readFromFile() {
        try {
            @SuppressLint("RestrictedApi") ObjectInputStream in = new ObjectInputStream(new FileInputStream
                    (new File(Objects.requireNonNull(getActivity(this.context)).getCacheDir() + File.separator + this.filename)));
            CryptoCurrency[] cachedData = cryptoResponseJsonAdapter.fromJson(Objects.requireNonNull((String) in.readObject()));
            readFromFileCallback(cachedData);
            in.close();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (OptionalDataException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (StreamCorruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    abstract void readFromFileCallback(CryptoCurrency[] cryptoCurrencies);
}
