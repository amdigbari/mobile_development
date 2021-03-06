package com.example.hw1;

import android.annotation.SuppressLint;
import android.content.Context;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.ArrayList;
import java.util.Objects;

import static com.google.android.material.internal.ContextUtils.getActivity;

public abstract class CacheTread extends Thread {
    private final boolean isWrite;
    private ArrayList<CryptoCurrency> cryptoCurrencies;
    private final Context context;
    private final String filename = "crypto_currencies.json";

    public CacheTread(Context context, ArrayList<CryptoCurrency> cryptoCurrencies) {
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
            JSONArray jsonArray = new JSONArray();
            for (CryptoCurrency cryptoCurrency : cryptoCurrencies) {
                jsonArray.put(getCryptoCurrency(cryptoCurrency));
            }
            @SuppressLint("RestrictedApi") ObjectOutput out = new ObjectOutputStream(new FileOutputStream
                    (new File(Objects.requireNonNull(getActivity(this.context)).getCacheDir(), "") + File.separator + this.filename));
            out.writeObject(jsonArray.toString());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    private void readFromFile() {
        try {
            @SuppressLint("RestrictedApi") ObjectInputStream in = new ObjectInputStream(new FileInputStream
                    (new File(Objects.requireNonNull(getActivity(this.context)).getCacheDir() + File.separator + this.filename)));
            CryptoCurrency[] cryptoCurrencies = new Gson().fromJson((String) in.readObject(), CryptoCurrency[].class);
            readFromFileCallback(cryptoCurrencies);
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

    private JSONObject getCryptoCurrency(CryptoCurrency cryptoCurrency) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("name", cryptoCurrency.name);
        jsonObject.put("id", cryptoCurrency.id);
        jsonObject.put("symbol", cryptoCurrency.symbol);
        JSONObject usd = new JSONObject()
                .put("price", cryptoCurrency.quote.USD.price)
                .put("percent_change_24h", cryptoCurrency.quote.USD.percent_change_24h)
                .put("percent_change_7d", cryptoCurrency.quote.USD.percent_change_7d)
                .put("percent_change_30d", cryptoCurrency.quote.USD.percent_change_30d);
        jsonObject.put("quote", new JSONObject().put("USD", usd));

        return jsonObject;
    }

    abstract void readFromFileCallback(CryptoCurrency[] cryptoCurrencies);
}
