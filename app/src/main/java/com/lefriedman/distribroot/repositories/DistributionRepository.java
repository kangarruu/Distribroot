package com.lefriedman.distribroot.repositories;

import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;
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

    public LiveData<DataSnapshot> getDataSnapshotLiveData(String key) {
        Log.d(TAG, "DistributionRepository getDataSnapshotLiveData: getting liveData<Snapshot> at key: " + key);
        return firebaseClient.getDataSnapshotLiveData(key);
    }

    public LiveData<String> makeRetrofitGeocodeApiCall(String name, String phone, String address, String city, String state, String zip, String apiKey){
        return firebaseClient.makeRetrofitGeocodeApiCall(name, phone, address, city, state, zip, apiKey);
    }



}