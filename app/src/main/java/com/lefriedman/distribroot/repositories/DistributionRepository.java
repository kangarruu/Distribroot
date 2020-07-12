package com.lefriedman.distribroot.repositories;

import androidx.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lefriedman.distribroot.livedata.FirebaseLiveData;
import com.lefriedman.distribroot.requests.DistribrootApiClient;

public class DistributionRepository {
    private static final String TAG = DistributionRepository.class.getSimpleName();

    private static DistributionRepository sInstance;
    private DistribrootApiClient distribrootApiClient;


    public DistributionRepository() {
        distribrootApiClient = new DistribrootApiClient().getInstance();
    }

    //Return a singleton repository object
    public static DistributionRepository getInstance() {
        if (sInstance == null) {
            sInstance = new DistributionRepository();
        } return sInstance;
    }

    public LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return distribrootApiClient.getDataSnapshotLiveData();
    }





}