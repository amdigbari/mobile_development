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
    private ArrayList<OHLC> ohlcs;
    private final boolean readOHLC;
    private final Context context;
    private final String filename;
    private boolean isWeek;

    public CacheTread(Context context, ArrayList<CryptoCurrency> cryptoCurrencies, String filename) {
        super();
        this.context = context;
        this.isWrite = true;
        this.cryptoCurrencies = cryptoCurrencies;
        this.filename = filename;
        this.readOHLC = false;
    }

    public CacheTread(Context context, String filename) {
        this.context = context;
        this.isWrite = false;
        this.filename = filename;
        this.readOHLC = false;
    }

    public CacheTread(ArrayList<OHLC> ohlcs, Context context, String filename, boolean isWeek) {
        this.ohlcs = ohlcs;
        this.readOHLC = true;
        this.context = context;
        this.filename = filename;
        this.isWrite = true;
        this.isWeek = isWeek;
    }

    public CacheTread(Context context, String filename, boolean readOHLC) {
        this.readOHLC = readOHLC;
        this.context = context;
        this.filename = filename;
        this.isWrite = false;
    }

    @Override
    public void run() {
        if (this.isWrite) {
            if (this.readOHLC) {
                writeToOHLCFile();
            } else {
                writeToFile();
            }
        } else {
            if (this.readOHLC) {
                readFromOHLCFile();
            } else {
                readFromFile();
            }
        }
    }

    private void writeToFile() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (CryptoCurrency cryptoCurrency : cryptoCurrencies) {
                jsonArray.put(getCryptoCurrency(cryptoCurrency));
            }
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream
                    (new File(context.getCacheDir() + File.separator + this.filename, "")));
            synchronized (CacheTread.class) {
                out.writeObject(jsonArray.toString());
                out.close();
            }
        } catch (IOException | JSONException | RuntimeException e) {
            e.printStackTrace();
        }
    }

    private void writeToOHLCFile() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (OHLC ohlc : ohlcs) {
                jsonArray.put(getOHLC(ohlc));
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("isWeek", this.isWeek);
            jsonObject.put("data", jsonArray.toString());
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream
                    (new File(context.getCacheDir() + File.separator + this.filename, "")));
            synchronized (CacheTread.class) {
                out.writeObject(jsonObject.toString());
                out.close();
            }

        } catch (IOException | JSONException | RuntimeException e) {
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
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        }
    }

    private void readFromOHLCFile() {
        try {
            @SuppressLint("RestrictedApi") ObjectInputStream in = new ObjectInputStream(new FileInputStream
                    (new File(Objects.requireNonNull(getActivity(this.context)).getCacheDir() + File.separator + this.filename)));
            CoinFragment.CacheResult result = new Gson().fromJson((String) in.readObject(), CoinFragment.CacheResult.class);
            readFromFileCallback(result.ohlcs, result.isWeek);
            in.close();
        } catch (ClassNotFoundException | IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private JSONObject getCryptoCurrency(CryptoCurrency cryptoCurrency) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("name", cryptoCurrency.getName());
        jsonObject.put("id", cryptoCurrency.getId());
        jsonObject.put("symbol", cryptoCurrency.getSymbol());
        JSONObject usd = new JSONObject()
                .put("price", cryptoCurrency.getQuote().getUSD().getPrice())
                .put("percent_change_1h", cryptoCurrency.getQuote().getUSD().getPercent_change_1h())
                .put("percent_change_24h", cryptoCurrency.getQuote().getUSD().getPercent_change_24h())
                .put("percent_change_7d", cryptoCurrency.getQuote().getUSD().getPercent_change_7d());
        jsonObject.put("quote", new JSONObject().put("USD", usd));

        return jsonObject;
    }

    private JSONObject getOHLC(OHLC ohlc) throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("price_open", ohlc.getPrice_open());
        jsonObject.put("price_close", ohlc.getPrice_close());
        jsonObject.put("price_high", ohlc.getPrice_high());
        jsonObject.put("price_low", ohlc.getPrice_low());
        jsonObject.put("time_period_start", ohlc.getTime_period_start());
        jsonObject.put("time_period_end", ohlc.getTime_period_end());
        jsonObject.put("time_open", ohlc.getTime_open());
        jsonObject.put("time_close", ohlc.getTime_close());
        jsonObject.put("volume_traded", ohlc.getVolume_traded());
        jsonObject.put("trades_count", ohlc.getTrades_count());

        return jsonObject;
    }

    abstract void readFromFileCallback(CryptoCurrency[] cryptoCurrencies);

    abstract void readFromFileCallback(OHLC[] ohlcs, boolean isWeek);
}
