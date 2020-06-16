package com.lefriedman.distribroot.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.lefriedman.distribroot.R;
import com.lefriedman.distribroot.databinding.ActivityFindDistributionBinding;

import static com.lefriedman.distribroot.util.Constants.CHECK_SETTINGS_REQUEST;

public class FindDistributionActivity extends BaseActivity {

    private static final String TAG = FindDistributionActivity.class.getSimpleName();

    private FusedLocationProviderClient mFusedLocationClient;
    private ActivityFindDistributionBinding mDataBinder;
    private LocationSettingsRequest.Builder mSettingsBuilder;
    private SettingsClient mSettingsClient;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_distribution);

        mDataBinder = DataBindingUtil.setContentView(this, R.layout.activity_find_distribution);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initSettingsBuilder();

    }

    //Create a settings builder to determine User's Location settings
    //Display dialog if User settings are not satisfied
    private void initSettingsBuilder() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(0)
                .setFastestInterval(0)
                .setNumUpdates(1);

        mSettingsBuilder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        mSettingsBuilder.setAlwaysShow(true);

        mSettingsClient = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsTask = mSettingsClient
                .checkLocationSettings(mSettingsBuilder.build());

        //Check the response:
        locationSettingsTask.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //Location settings are satisfied, get last location
                getLastLocation();
            }
        });

        locationSettingsTask.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                if (exception instanceof ResolvableApiException){
                    //Show user dialog to resolve location settings
                    try {
                        ResolvableApiException resolvableException = (ResolvableApiException) exception;
                        resolvableException.startResolutionForResult(FindDistributionActivity.this,
                                CHECK_SETTINGS_REQUEST);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CHECK_SETTINGS_REQUEST:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        getLastLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        //Settings Dialog was displayed but User didn't change settings
                        Toast.makeText(FindDistributionActivity.this, "This application requires GPS to work properly. Enable Location in settings to continue.",
                                Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
                break;
        }

    }

    private void getLastLocation() {
        if(ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location location = task.getResult();
                    if (location == null) {
                        //If location returns null, create a location request
                        requestNewLocationData();
                    } else {
                        Log.d(TAG, "getLastLocation:" + "" + location.getLatitude() + " " + location.getLongitude());
                    }
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        Log.d(TAG, "requestNewLocationData called. LocationRequest == " + "" + locationRequest);
        mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    //Implement the LocationCallback interface
     private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                Log.d(TAG, "onLocationResult: locationResult == null");
                return;
            }
            Location lastLocation = locationResult.getLastLocation();
            Log.d(TAG, "onLocationResult:" + " " + lastLocation.getLatitude() + " " + lastLocation.getLongitude());
        }
    };


    @Override
    protected void onPause() {
        super.onPause();

    }
}