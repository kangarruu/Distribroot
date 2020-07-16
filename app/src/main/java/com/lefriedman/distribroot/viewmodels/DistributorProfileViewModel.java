package com.lefriedman.distribroot.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.lefriedman.distribroot.R;
import com.lefriedman.distribroot.repositories.DistributionRepository;

public class DistributorProfileViewModel extends AndroidViewModel {
    private static final String TAG = DistributorProfileViewModel.class.getSimpleName();

    private DistributionRepository mRepository;
    private String apiKey;

    public DistributorProfileViewModel(@NonNull Application application) {
        super(application);
        apiKey = getApplication().getString(R.string.maps_api_key);
        mRepository = DistributionRepository.getInstance();
    }


    public LiveData<String> makeRetrofitGeocodeApiCall(String name, String phone, String address, String city, String state, String zip) {
        Log.d(TAG, "DistributorProfileViewModel calling makeRetrofitGeocodeApiCall()");
        return mRepository.makeRetrofitGeocodeApiCall(name, phone, address, city, state, zip, apiKey);

    }


}
