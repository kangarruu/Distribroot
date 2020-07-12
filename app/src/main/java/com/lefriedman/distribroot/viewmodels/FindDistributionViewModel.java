package com.lefriedman.distribroot.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lefriedman.distribroot.livedata.FirebaseLiveData;
import com.lefriedman.distribroot.repositories.DistributionRepository;

public class FindDistributionViewModel extends ViewModel {
    private static final String TAG = FindDistributionViewModel.class.getSimpleName();

    private DistributionRepository mRepository;

    public FindDistributionViewModel() {
        mRepository = DistributionRepository.getInstance();
    }

    //get the liveData object from the repository
    public LiveData<DataSnapshot> getDataSnapshotLiveData() {
        return mRepository.getDataSnapshotLiveData();
    }
}
