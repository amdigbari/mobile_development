package com.example.hw2.repository;

import android.app.Application;
import android.os.AsyncTask;

import com.example.hw2.model.Place;
import com.example.hw2.repository.db.AppDao;
import com.example.hw2.repository.db.AppDatabase;

import java.util.List;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppRepository {
    private static final String BASE_URL = "";
    private ApiService apiService;
    private Retrofit retrofit;

    public ResponseBody forwardGeocode(String search) {
        return apiService.forwardGeocode(search);//todo: should go to background
    }

}
