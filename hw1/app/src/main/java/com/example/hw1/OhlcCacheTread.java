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

public abstract class OhlcCacheTread extends Thread{
    private final boolean isWrite;
    private ArrayList<OHLC> ohlcs;
    private final Context context;
    private final String filename = "ohlc.json";

    public OhlcCacheTread(Context context, ArrayList<OHLC> ohlcs) {
        this.context = context;
        this.isWrite = true;
        this.ohlcs = ohlcs;
    }

    public OhlcCacheTread(Context context) {
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
            for (OHLC ohlc : ohlcs) {
                jsonArray.put(getOhlc(ohlc));
            }
            ObjectOutput out = new ObjectOutputStream(new FileOutputStream
                    (new File(context.getCacheDir(), "") + File.separator + this.filename));
            synchronized (CacheTread.class) {
                out.writeObject(jsonArray.toString());
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
            OHLC[] ohlcsArr = new Gson().fromJson((String) in.readObject(), OHLC[].class);
            readFromFileCallback(ohlcsArr);
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

    private JSONObject getOhlc(OHLC ohlc) throws JSONException {
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

    abstract void readFromFileCallback(OHLC[] ohlcs);
}
