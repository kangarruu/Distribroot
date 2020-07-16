package com.lefriedman.distribroot.util;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lefriedman.distribroot.R;

public class Constants {

    //FirebaseAuth flag
    public static final int RC_SIGN_IN = 100;
    //MainActivity location permission
    public static final int LOCATION_PERMISSION_REQUEST = 101;
    //MainActivity Google Play Services error dialog
    public static final int ERROR_DIALOG_REQUEST = 102;
    //FindDistributionActivity locationSettingsRequest
    public static final int CHECK_SETTINGS_REQUEST = 103;
    //Geolocation API Base URL
    public static final String BASE_URL = "https://maps.googleapis.com/";
    //Geofire reference
    public static final DatabaseReference DISTRIBUTOR_LOCATION_REF = FirebaseDatabase.getInstance()
            .getReference().child("distributor_location");
    public static final DatabaseReference DISTRIBUTORS_REF = FirebaseDatabase.getInstance()
            .getReference().child("distributors");



}
