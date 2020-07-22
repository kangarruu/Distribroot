package com.lefriedman.distribroot.viewmodels;

import android.location.Location;
import android.util.Log;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.lefriedman.distribroot.models.Distributor;
import com.lefriedman.distribroot.repositories.DistributionRepository;

import static com.lefriedman.distribroot.util.Constants.DISTRIBUTOR_LOCATION_REF;

public class FindDistributionViewModel extends ViewModel {
    private static final String TAG = FindDistributionViewModel.class.getSimpleName();

    private DistributionRepository mRepository;
    private GeoFire mGeoFire;
    private LiveData<Distributor> mDistributorLiveData;
    private LiveData<DataSnapshot> dataSnapshotLiveData;
    private LiveData<DataSnapshot> dataSnapshot;
    private MutableLiveData<LatLng> mDistributorLatLng;



    public FindDistributionViewModel() {
        mRepository = DistributionRepository.getInstance();
        mGeoFire = new GeoFire(DISTRIBUTOR_LOCATION_REF);
        mDistributorLatLng = new MutableLiveData<>();
    }

    public void makeGeoQuery(Location result) {
        GeoQuery geoQuery = mGeoFire.queryAtLocation(new GeoLocation(result.getLatitude(), result.getLongitude()),10);
        Log.d(TAG, "makeGeoQuery: Making geoQuery" + geoQuery.toString());

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                mDistributorLatLng.setValue(new LatLng(location.latitude, location.longitude));
                Log.d(TAG, "onKeyEntered: mDistributorLatlang = " + mDistributorLatLng.getValue());

                mRepository.makeFirebaseDistributorQuery(key);
                Log.d(TAG, "makeGeoQuery onKeyEntered: making FirebaseQuery for key: " + key );

//                mDistributorLiveData = Transformations.map(dataSnapshotLiveData, new Deserializer());
//                Log.d(TAG, "onKeyEntered: mDistributorLiveData: " + mDistributorLiveData.getValue());
//

            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }

        });
    }


    public LiveData<LatLng> getDistributorLatLng(){
        Log.d(TAG, "getDistributorLatLng: Returning mDistributorLatLng:  " + mDistributorLatLng.getValue());
        return mDistributorLatLng;
    }

    public LiveData<DataSnapshot> getDistributorSnapshotLiveData() {
        Log.d(TAG, "ViewModel getDistributorSnapshotLiveData: getting liveData<Snapshot> from Repository ");
        return mRepository.getDistributorSnapshotLiveData();
    }


    private class Deserializer implements Function<DataSnapshot, Distributor> {

        @Override
        public Distributor apply(DataSnapshot dataSnapshot) {
            return dataSnapshot.getValue(Distributor.class);
        }
    }
}