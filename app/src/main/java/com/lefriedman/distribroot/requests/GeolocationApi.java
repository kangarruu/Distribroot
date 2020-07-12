package com.lefriedman.distribroot.requests;

import com.lefriedman.distribroot.models.retrofit.ResultWrapper;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GeolocationApi {

    //Simple API interface to call the geolocation API with an address and return a result that includes the latitude and longitude
    @GET("maps/api/geocode/json")
    Call<ResultWrapper> getGeocode(
            @Query("address") String address,
            @Query("key") String key
    );




}
