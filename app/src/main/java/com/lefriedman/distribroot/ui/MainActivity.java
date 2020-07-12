package com.lefriedman.distribroot.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lefriedman.distribroot.R;
import com.lefriedman.distribroot.databinding.ActivityMainBinding;

import java.util.Arrays;

import static com.lefriedman.distribroot.util.Constants.ERROR_DIALOG_REQUEST;
import static com.lefriedman.distribroot.util.Constants.LOCATION_PERMISSION_REQUEST;
import static com.lefriedman.distribroot.util.Constants.RC_SIGN_IN;

public class MainActivity extends BaseActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ActivityMainBinding mDataBinder;
    private boolean isLocationPermissionGranted = false;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Data binding
        mDataBinder = DataBindingUtil.setContentView(this, R.layout.activity_main);

        //Initialize Firebase authentication
        InitFirebaseAuth();

        //Set the button click listeners
        attachFindDistributionClickListener();
        attachStartDistributionClickListener();
        attachSignOutClickListener();

        //Create preference boolean to check if location dialog was previously shown
        sharedPref = getApplicationContext().getSharedPreferences("SharedPref", MODE_PRIVATE);
        editor = sharedPref.edit();
        editor.putBoolean("isFirstTimeRequest", true).apply();

    }

    private void attachStartDistributionClickListener() {
        mDataBinder.welcomeDistributorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (areGooglePlayServicesEnabled() && isLocationPermissionGranted){
                    Log.d(TAG, "attachStartDistributionClickListener: launching new activity ");
                    Intent intent = new Intent(MainActivity.this, DistributorProfileActivity.class);
                    startActivity(intent);
                } else {
                    requestLocationPermissions();
                }

            }
        });
    }

    //If all location permissions are granted, launch activity to find local distribution using User's location
    private void attachFindDistributionClickListener(){
        mDataBinder.welcomeUserBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (areGooglePlayServicesEnabled() && isLocationPermissionGranted){
                    Log.d(TAG, "attachFindDistributionClickListener: launching new activity ");
                    Intent intent = new Intent(MainActivity.this, FindDistributionActivity.class);
                    startActivity(intent);
                    } else {
                        requestLocationPermissions();
                    }
                }
            });
        }

    private void attachSignOutClickListener(){
        mDataBinder.signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AuthUI.getInstance() != null) {
                    AuthUI.getInstance().signOut(MainActivity.this);
                }
            }
        });
    }

    private void requestLocationPermissions() {
        //Request location permissions to get User's location.
        if(ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //If shouldShowRequestPermissionRationale returns true then user has not accepted permissions
            //Display request dialog
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                //Display dialog and wait for user interaction
                displayFineLocationRequestDialog();
                //If shouldShowRequestPermissionRationale returns false, check if request is being made for the first time
                //and request permissions
                //If isFirstTimeRequest is false then User has selected "Never ask again", display error Toast
            } else {
                if (sharedPref.getBoolean("isFirstTimeRequest", true)) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
                    editor.putBoolean("isFirstTimeRequest", false).commit();
                } else {
                    Toast.makeText(this, R.string.main_location_request_disabled_toast_msg, Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Log.d(TAG, "requestLocationPermissions: Permissions have been granted ");
            isLocationPermissionGranted = true;
        }
    }

    private void displayFineLocationRequestDialog() {
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(R.string.main_location_request_dialog_msg)
                .setNegativeButton(R.string.main_location_request_dialog_cancel_btn_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                })
                .setPositiveButton(R.string.main_location_request_dialog_yes_btn_txt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
                    }
                });

        final AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        isLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                //Check legth of results arrays. if request is cancelled, result are empty.
                if (grantResults.length > 0
                        && grantResults [0] == PackageManager.PERMISSION_GRANTED){
                    isLocationPermissionGranted = true;
                    Intent intent = new Intent(MainActivity.this, FindDistributionActivity.class);
                    startActivity(intent);
                }
        }
    }

    //Check that the User has Google Play Services up to date
    private boolean areGooglePlayServicesEnabled() {
        //get singelton object that has the isGooglePlayServicesAvailable method
        int googleAvailability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        //if result code is SUCCESS then GPS APK is up-to-date
        if (googleAvailability == ConnectionResult.SUCCESS) {
            Log.d(TAG, "areGooglePlayServicesEnabled() returning true, GPS up to date");
            return true;
            //If result code isnt SUCCESS User needs to install update, call error Dialog
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(googleAvailability)){
            Log.d(TAG, "areGooglePlayServicesEnabled() returning error, display error dialog");
            Dialog errorDialog = GoogleApiAvailability.getInstance().getErrorDialog(
                    this, googleAvailability, ERROR_DIALOG_REQUEST);
            errorDialog.show();
        } else {
            Toast.makeText(this, R.string.main_google_play_not_enabled_error_toast, Toast.LENGTH_SHORT).show();
        }
        return false;
    }


//
//    private void displayGPSRequestDialog() {
//        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//        dialogBuilder.setMessage(R.string.main_gps_request_dialog_msg)
//                .setNegativeButton(R.string.main_location_request_dialog_cancel_btn_txt, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        return;
//                    }
//                })
//                .setPositiveButton(R.string.main_location_request_dialog_yes_btn_txt, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        Intent enableGpsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                        startActivityForResult(enableGpsIntent, GPS_PERMISSION_REQUEST_ID);
//                        return;
//                    }
//                });
//
//        final AlertDialog alertDialog = dialogBuilder.create();
//        alertDialog.show();
//    }


    private void InitFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();

        //Set an AuthStateListener to listen to login changes
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //User is signed in, proceed to mainActivity
                    Log.d(TAG, "onAuthStateChanged: User authenticated");

                    onSignedInInitialize(user.getDisplayName());

                } else {
                    //User is signed out
                    //Set custom login layout
                    AuthMethodPickerLayout loginLayout = new AuthMethodPickerLayout
                            .Builder(R.layout.auth_login_custom_layout)
                            .setGoogleButtonId(R.id.login_google_btn)
                            .setEmailButtonId(R.id.login_email_btn)
                            .setPhoneButtonId(R.id.login_phone_btn)
                            .build();

                    //Display sign in options
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.GoogleBuilder().build(),
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.PhoneBuilder().build()))
                                    .setAuthMethodPickerLayout(loginLayout)
                                    .setTheme(R.style.AppTheme)
                                    .build(),
                            RC_SIGN_IN);
                }

            }
        };
    }

    private void onSignedInInitialize(String displayName) {
//        mUserName = displayName;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RC_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(MainActivity.this, "Successfully signed in", Toast.LENGTH_SHORT).show();
                } else if (requestCode == RESULT_CANCELED) {
                    Log.d(TAG, "onActivityResult: Sign in cancelled");
                    //Finish activity to allow for backpress
                    finish();
                }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }



}
