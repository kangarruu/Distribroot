package com.lefriedman.distribroot.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lefriedman.distribroot.R;
import com.lefriedman.distribroot.databinding.ActivityDistributorProfileBinding;
import com.lefriedman.distribroot.viewmodels.DistributorProfileViewModel;


public class DistributorProfileActivity extends BaseActivity {

    private static final String TAG = DistributorProfileActivity.class.getSimpleName();

    ActivityDistributorProfileBinding mDataBinder;
    private FirebaseDatabase mFirebaseDb;
    private DatabaseReference mDistributorDbReference;
    private ChildEventListener mChildEventListener;
    private DistributorProfileViewModel mViewmodel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distributor_profile);

        //Databinding
        mDataBinder = DataBindingUtil.setContentView(this, R.layout.activity_distributor_profile);

        //Set up ViewModel
        mViewmodel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(this.getApplication())).get(DistributorProfileViewModel.class);

        attachSubmitClickListener();

        //Firebase database setup
        mFirebaseDb = FirebaseDatabase.getInstance();
        mDistributorDbReference = mFirebaseDb.getReference().child("distributors");
    }


    private void attachSubmitClickListener(){
        mDataBinder.userSubmitFormBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extractDistributorData();
            }
        });
    }

    private void extractDistributorData(){
        //Collect distributor data
        String name = mDataBinder.distributorNameEditText.getText().toString();
        String phone = mDataBinder.distributorPhoneEditText.getText().toString();
        String address = mDataBinder.distributorAddressEditText.getText().toString();
        String city = mDataBinder.distributorCityEditText.getText().toString();
        String state = mDataBinder.distributorStateEditText.getText().toString();
        String zip = mDataBinder.distributorZipEditText.getText().toString();

        //Save address to Firebase and generate the latitude and longitude coordinates using the Geolocation API
        try {
            mViewmodel.makeRetrofitGeocodeApiCall(name, phone, address, city, state, zip).observe(this, new Observer<String>() {
                @Override
                public void onChanged(String toastMessage) {
                    Toast.makeText(DistributorProfileActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void attachDbReadListener(){
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                    Log.d(TAG, "Firebase ChildEventListener onChildAdded triggered" + dataSnapshot.getKey());
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {               }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d(TAG, " Firebase ChildEventListener() onCancelled: " + databaseError);
                }
            };

            //Attach to Db reference
            mDistributorDbReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDbReadListener(){
        if(mChildEventListener != null){
            mDistributorDbReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        attachDbReadListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachDbReadListener();
    }

}

