package com.lefriedman.distribroot.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.PersistableBundle;
import android.util.Log;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lefriedman.distribroot.R;
import com.lefriedman.distribroot.models.Distributor;
import com.lefriedman.distribroot.viewmodels.FindDistributionViewModel;

import java.util.ArrayList;

import static com.lefriedman.distribroot.util.Constants.CHECK_SETTINGS_REQUEST;

public class FindDistributionActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = FindDistributionActivity.class.getSimpleName();

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationSettingsRequest.Builder mSettingsBuilder;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private Location mUserLocation;
    private GoogleMap mMap;
    private FirebaseDatabase mFirebaseDb;
    private GeoFire mGeoFire;
    private LatLng mDistributorLatLng;
    private ArrayList<Distributor> mDistributorList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_distribution);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null){
            mapFragment.getMapAsync(this);
        }

        //Firebase database setup
        mFirebaseDb = FirebaseDatabase.getInstance();
        mGeoFire = new GeoFire(mFirebaseDb.getReference().child("distributor_location"));

    }

    //Create a settings builder to determine User's Location settings
    //Display dialog if User settings are not satisfied
    //If settings are satisfied, get User's location to display on the map
    private void initSettingsBuilder() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
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
        if(ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    mUserLocation = task.getResult();
                    Log.d(TAG, "onComplete: mUserLocation == " + mUserLocation);
                    setMapCameraView();
                    mMap.setMyLocationEnabled(true);
                    if (mUserLocation == null) {
                        //If location returns null, create a location request
                        requestNewLocationData();
                    } else {
                        //TODO Use this location to display the user on a map and locate distributions close by
                        Log.d(TAG, "getLastLocation:" + "" + mUserLocation.getLatitude() + " " + mUserLocation.getLongitude());
                        //Create new geoQuery using the User's location
                        GeoQuery geoQuery = mGeoFire.queryAtLocation(new GeoLocation(mUserLocation.getLatitude(), mUserLocation.getLongitude()),10);
                        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                            @Override
                            public void onKeyEntered(String key, final GeoLocation location) {

                                mFirebaseDb.getReference().child("distributors").child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        //Retrieve the distributor at the given key that satisfies the GeoQuery and add it to a list
                                        Distributor distributor = dataSnapshot.getValue(Distributor.class);
                                        mDistributorList.add(distributor);
                                        Log.d(TAG, "onDataChange: distributorList: " + mDistributorList);

                                        if (location != null) {
                                            mDistributorLatLng = new LatLng(location.latitude, location.longitude);
                                            Log.d(TAG, "onKeyEntered: mDistributorLatlang = " + mDistributorLatLng);
                                        }

                                        mMap.addMarker(new MarkerOptions()
                                                .position(mDistributorLatLng)
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                                .title(distributor.getName()));

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });



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

    //Implement the LocationCallback interface
     private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                Log.d(TAG, "onLocationResult: locationResult == null");
                return;
            }
            mUserLocation = locationResult.getLastLocation();
            setMapCameraView();
        }
    };


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
        if (mUserLocation != null){
            LatLng latLng = new LatLng(mUserLocation.getLatitude(), mUserLocation.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title(getString(R.string.distribution_map_marker_text)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
        } else {
            Log.d(TAG, "setMapCameraView: userLocation == null");
        }

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }


}