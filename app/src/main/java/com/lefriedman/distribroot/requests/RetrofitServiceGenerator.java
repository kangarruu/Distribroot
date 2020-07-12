package com.lefriedman.distribroot.requests;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.lefriedman.distribroot.util.Constants.BASE_URL;

public class RetrofitServiceGenerator {

    //Create a Retrofit singelton
    private static Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create());

    private static Retrofit retrofit = retrofitBuilder.build();

    //Create an instance of the geolocationApi
    private static GeolocationApi geolocationApi = retrofit.create(GeolocationApi.class);

    //Return the instance
    public static GeolocationApi getGeolocationApi() {
        return geolocationApi;
    }

}
