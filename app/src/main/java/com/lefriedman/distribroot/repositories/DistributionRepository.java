package com.lefriedman.distribroot.repositories;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.android.gms.maps.model.MarkerOptions;
import com.lefriedman.distribroot.requests.FirebaseClient;

public class DistributionRepository {
    private static final String TAG = DistributionRepository.class.getSimpleName();

    private static DistributionRepository sInstance;
    private FirebaseClient firebaseClient;

    public DistributionRepository() {
        firebaseClient = new FirebaseClient().getInstance();
    }

    //Return a singleton repository object
    public static DistributionRepository getInstance() {
        if (sInstance == null) {
            sInstance = new DistributionRepository();
        } return sInstance;
    }

    //Send the retrofit params down to the FirebaseClient for making Retrofit GeoApi query
    public LiveData<String> makeRetrofitGeocodeApiCall(String name, String phone, String address, String city, String state, String zip, String apiKey){
        return firebaseClient.makeRetrofitGeocodeApiCall(name, phone, address, city, state, zip, apiKey);
    }

    //Send a result of GetLastLocation() method to FirebaseClient to make a GeoQuery
    public static void makeGeoQuery(Location location) {
        FirebaseClient.makeGeoQuery(location);
    }

    //Return MarkerOptions LiveData from FirebaseClient for the Distributors that satisfy the GeoQuery
    public LiveData<MarkerOptions> getDistributorMarkerOptionsLiveData() {
        return FirebaseClient.getDistributorMarkerOptionsLiveData();
    }

}