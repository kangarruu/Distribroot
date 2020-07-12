package com.lefriedman.distribroot.requests;

import androidx.lifecycle.LiveData;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lefriedman.distribroot.livedata.FirebaseLiveData;


public class DistribrootApiClient {
    private static final String TAG = DistribrootApiClient.class.getSimpleName();

    private static DistribrootApiClient sInstance;
    private FirebaseLiveData mFirebaseLiveData;

    private static final DatabaseReference DISTRIBUTORS_REF = FirebaseDatabase.getInstance()
            .getReference().child("distributors");

    //    private static final DatabaseReference DISTRIBUTOR_LOCATION_REF = FirebaseDatabase.getInstance().getReference().child("distributor_location");

    public DistribrootApiClient() {
        mFirebaseLiveData = new FirebaseLiveData(DISTRIBUTORS_REF);
    }

    //Return singleton DistribrootApiClient
    public static DistribrootApiClient getInstance() {
        if(sInstance == null){
            sInstance = new DistribrootApiClient();
        } return sInstance;
    }

    public LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return mFirebaseLiveData;
    }





}
