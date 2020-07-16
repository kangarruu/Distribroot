package com.lefriedman.distribroot.viewmodels;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.firebase.geofire.GeoFire;
import com.google.firebase.database.DataSnapshot;
import com.lefriedman.distribroot.livedata.DataSnapshotLiveData;
import com.lefriedman.distribroot.models.Distributor;
import com.lefriedman.distribroot.repositories.DistributionRepository;

import static com.lefriedman.distribroot.util.Constants.DISTRIBUTOR_LOCATION_REF;

public class FindDistributionViewModel extends ViewModel {
    private static final String TAG = FindDistributionViewModel.class.getSimpleName();

    private DistributionRepository mRepository;
    private GeoFire mGeoFire;
    private LiveData<DataSnapshot> mDistributorLiveData;

    public FindDistributionViewModel() {
        mRepository = DistributionRepository.getInstance();
        mGeoFire = new GeoFire(DISTRIBUTOR_LOCATION_REF);
        mDistributorLiveData = mRepository.getDataSnapshotLiveData();

    }

    //get the liveData object from the repository
    public LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return mDistributorLiveData;
    }


    //Viewmodel has instance of repository and calls appropriate methods for getting data from local database or from API

//    public void makeGeoQuery(Location result) {
//        GeoQuery geoQuery = mGeoFire.queryAtLocation(new GeoLocation(result.getLatitude(), result.getLongitude()),10);
//        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
//            @Override
//            public void onKeyEntered(String key, final GeoLocation location) {
//                mFirebaseDb.getReference().child("distributors").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        //Retrieve the distributor at the given key that satisfies the GeoQuery and add its marker to the map
//                        Distributor distributor = dataSnapshot.getValue(Distributor.class);
//
//                        if (location != null) {
//                            mDistributorLatLng = new LatLng(location.latitude, location.longitude);
//                            Log.d(TAG, "onKeyEntered: mDistributorLatlang = " + mDistributorLatLng);
//                        }
//                    }
//
//
//                });
//            }
//
//            @Override
//            public void onKeyExited(String key) {
//
//            }
//
//            @Override
//            public void onKeyMoved(String key, GeoLocation location) {
//
//            }
//
//            @Override
//            public void onGeoQueryReady() {
//
//            }
//
//            @Override
//            public void onGeoQueryError(DatabaseError error) {
//
//            }
//
//            ;
//        });
//    }
}