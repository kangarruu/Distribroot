package com.lefriedman.distribroot.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.lefriedman.distribroot.R;
import com.lefriedman.distribroot.models.Distributor;
import com.lefriedman.distribroot.viewmodels.FindDistributionViewModel;

import static com.lefriedman.distribroot.util.Constants.CHECK_SETTINGS_REQUEST;

public class FindDistributionActivity extends AppCompatActivity implements OnMapReadyCallback, OnInfoWindowClickListener {

    private static final String TAG = FindDistributionActivity.class.getSimpleName();

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationSettingsRequest.Builder mSettingsBuilder;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private Location mUserLocation;
    private GoogleMap mMap;
    private LatLng mDistributorLatLng;
    private String mDistributorName;
    private String mDistributorAddress;
    private Marker mClickedMarker;
    private Marker mUserLocationMarker;
    private Boolean isCameraViewSet = false;
    private FindDistributionViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_distribution);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        mViewModel = new ViewModelProvider(this).get(FindDistributionViewModel.class);

        LiveData<MarkerOptions> mDistributorMarkerOptions = mViewModel.getDistributorMarkerOptionsLiveData();
        mDistributorMarkerOptions.observe(this, new Observer<MarkerOptions>() {
            @Override
            public void onChanged(MarkerOptions markerOptions) {
                mMap.addMarker(markerOptions);
            }
        });
    }

    //Create a settings builder to determine User's Location settings
    //Display dialog if User settings are not satisfied
    //If settings are satisfied, get User's location to display on the map
    private void initSettingsBuilder() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setFastestInterval(5000);

        mSettingsBuilder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        mSettingsBuilder.setAlwaysShow(true);

        mSettingsClient = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsTask = mSettingsClient
                .checkLocationSettings(mSettingsBuilder.build());

        //add the listeners:
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
                if (exception instanceof ResolvableApiException) {
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
            //Results of the locationSettingsTask, if satisfied get location, if not, display error Toast
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
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    mUserLocation = task.getResult();
                    if (mUserLocation == null) {
                        //If location returns null, create a location request
                        requestNewLocationData();
                    } else {
                        //Otherwise send this info to ViewModel to make a GeoFire call and update the Livedata with local distributors
                        Log.d(TAG, "getLastLocation:" + "" + mUserLocation.getLatitude() + " " + mUserLocation.getLongitude());
                        mViewModel.makeGeoQuery(mUserLocation);
                    }

                    if (!isCameraViewSet) {
                        setMapCameraView();
                        mMap.setMyLocationEnabled(true);
                    }

                }
            }
        });

    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {
        Log.d(TAG, "requestNewLocationData called. LocationRequest == " + "" + mLocationRequest);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, locationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initSettingsBuilder();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Stop location updates if activity isnt in the foreground
        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }

    }

    //Limit the camera view to the User's location
    private void setMapCameraView() {
        Log.d(TAG, "setMapCameraView: mUserLocation == " + mUserLocation);
        if (mUserLocation != null) {
            LatLng latLng = new LatLng(mUserLocation.getLatitude(), mUserLocation.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 13));
            if (mUserLocationMarker == null){
                mUserLocationMarker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                        .title(getString(R.string.distribution_map_marker_text)));
            } else {
                mUserLocationMarker.setPosition(latLng);
            }
            isCameraViewSet = true;
        } else {
            Log.d(TAG, "setMapCameraView: userLocation == null");
        }
    }

    private void addDistributorMapMarkers() {
        try {
            mMap.addMarker(new MarkerOptions()
                    .position(mDistributorLatLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .title(mDistributorName)
                    .snippet(mDistributorAddress));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnInfoWindowClickListener(this);
        mMap.setContentDescription(getString(R.string.find_map_content_description));
    }


    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(FindDistributionActivity.this, "Testing infoWindow " + marker.getTitle(), Toast.LENGTH_LONG).show();

    }

    //Implement the LocationCallback interface
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                Log.d(TAG, "onLocationResult: locationResult == null");
                return;
            }
            mUserLocation = locationResult.getLastLocation();

            if (!isCameraViewSet) {
                setMapCameraView();
                isCameraViewSet = true;
            }

        }
    };


}