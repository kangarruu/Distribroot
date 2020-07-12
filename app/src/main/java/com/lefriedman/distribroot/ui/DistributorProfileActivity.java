package com.lefriedman.distribroot.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lefriedman.distribroot.R;
import com.lefriedman.distribroot.databinding.ActivityDistributorProfileBinding;
import com.lefriedman.distribroot.models.Distributor;
import com.lefriedman.distribroot.models.retrofit.Result;
import com.lefriedman.distribroot.models.retrofit.ResultWrapper;
import com.lefriedman.distribroot.requests.GeolocationApi;
import com.lefriedman.distribroot.requests.RetrofitServiceGenerator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DistributorProfileActivity extends BaseActivity {

    private static final String TAG = DistributorProfileActivity.class.getSimpleName();

    ActivityDistributorProfileBinding mDataBinder;
    private FirebaseDatabase mFirebaseDb;
    private DatabaseReference mDistributorDbReference;
    private ChildEventListener mChildEventListener;
    private GeoFire mGeoFire;
    private String mPushId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distributor_profile);

        //Databinding
        mDataBinder = DataBindingUtil.setContentView(this, R.layout.activity_distributor_profile);

        attachSubmitClickListener();

        //Firebase database setup
        mFirebaseDb = FirebaseDatabase.getInstance();
        mDistributorDbReference = mFirebaseDb.getReference().child("distributors");
        mGeoFire = new GeoFire(mFirebaseDb.getReference().child("distributor_location"));
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

        //Generate the latitude and longitude coordinates using the Geocoding API
        try {
            makeRetrofitGeocodeApiCall(name, phone, address, city, state, zip);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void makeRetrofitGeocodeApiCall(String name, String phone, String address, String city, String state, String zip) {

        String concatAddress = address + " " + city + " " + state + " " + zip;
        Log.d(TAG, "makeRetrofitGeocodeApiCall: making retrofit call. Address = " + concatAddress);

        //Get an instance of the Geolocation Api
        GeolocationApi geolocationApi = RetrofitServiceGenerator.getGeolocationApi();
        Call<ResultWrapper> geoResult = geolocationApi.getGeocode(concatAddress, getString(R.string.maps_api_key));

        geoResult.enqueue(new Callback<ResultWrapper>() {
            @Override
            public void onResponse(Call<ResultWrapper> call, Response<ResultWrapper> response) {
                if (response.isSuccessful()){
                    Log.d(TAG, "GeoResult onResponse: georesult: " + response.body().toString());
                    List<Result> resultList = response.body().getResults();
                    String placeId = resultList.get(0).getPlaceId();
                    Double lat = resultList.get(0).getGeometry().getLocation().getLat();
                    Double lng =  resultList.get(0).getGeometry().getLocation().getLng();

                    //Create new distributor object to add to the DB
                    Distributor distributor = new Distributor(name, phone, address, city, state, zip, placeId);
                    //Generate a pushId in Firebase Database for use in GeoFire
                    mPushId = mDistributorDbReference.push().getKey();
                    //Set the distributor & GeoFire data using the new key
                    mDistributorDbReference.child(mPushId).setValue(distributor);
                    mGeoFire.setLocation(mPushId, new GeoLocation(lat, lng), new GeoFire.CompletionListener() {
                        @Override
                        public void onComplete(String key, DatabaseError error) {
                            if (error !=null){
                                Log.d(TAG, "GeoFire onComplete: There was an error adding distributor geoCoordinates to GeoFire ");
                            }else {
                                Log.d(TAG, "GeoFire onComplete: new distributor geoCoordinates saved to GeoFire successfully");
                            }
                        }
                    });


                } else {
                    try {
                        Log.d(TAG, "makeRetrofitGeocodeApiCall() onResponse: GeoResult error = " + response.errorBody().toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResultWrapper> call, Throwable t) {
                Log.d(TAG, "makeRetrofitGeocodeApiCall() onFailure: Retrofit call failed: " + t);
            }
        });

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

