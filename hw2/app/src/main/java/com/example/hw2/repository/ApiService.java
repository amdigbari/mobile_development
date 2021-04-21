package com.example.hw2.repository;

import com.example.hw2.model.Place;

import java.util.List;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("geocoding/v5/mapbox.places/{place}.json")
    ResponseBody forwardGeocode(@Path("place") String place, @Query("access_token") String token);
}
