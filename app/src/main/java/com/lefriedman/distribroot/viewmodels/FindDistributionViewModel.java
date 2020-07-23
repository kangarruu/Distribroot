package com.lefriedman.distribroot.viewmodels;

import android.location.Location;
import android.util.Log;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.lefriedman.distribroot.models.Distributor;
import com.lefriedman.distribroot.repositories.DistributionRepository;
import com.lefriedman.distribroot.requests.FirebaseClient;

import static com.lefriedman.distribroot.util.Constants.DISTRIBUTOR_LOCATION_REF;

public class FindDistributionViewModel extends ViewModel {
    private static final String TAG = FindDistributionViewModel.class.getSimpleName();

    private DistributionRepository mRepository;

    public FindDistributionViewModel() {
        mRepository = DistributionRepository.getInstance();
    }

    //Send result of FindDistributionActivity getLastLocation() method to FirebaseClient through the repository to make a GeoQuery
    public void makeGeoQuery(Location location) {
        mRepository.makeGeoQuery(location);
    }

    //Retrieve the MarkerOptions LiveData from the repository for the Distributors that satisfy the GeoQuery made in the FirebaseClient
    public LiveData<MarkerOptions> getDistributorMarkerOptionsLiveData() {
        return mRepository.getDistributorMarkerOptionsLiveData();
    }


}