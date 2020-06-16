package com.lefriedman.distribroot.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lefriedman.distribroot.R;
import com.lefriedman.distribroot.databinding.ActivityMainBinding;
import com.lefriedman.distribroot.databinding.ActivityUserProfileBinding;
import com.lefriedman.distribroot.models.User;

import java.util.Arrays;

import static com.lefriedman.distribroot.util.Constants.RC_SIGN_IN;

public class UserProfileActivity extends BaseActivity{

    private static final String TAG = UserProfileActivity.class.getSimpleName();

    ActivityUserProfileBinding mDataBinder;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseDatabase mFirebaseDb;
    private DatabaseReference mUserDbReference;
    private ChildEventListener mChildEventListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //Databinding
        mDataBinder = DataBindingUtil.setContentView(this, R.layout.activity_user_profile);

        //Initialize Firebase authentication
        InitFirebaseAuth();

        attachSubmitClickListener();

        //Firebase database setup
        mFirebaseDb = FirebaseDatabase.getInstance();
        mUserDbReference = mFirebaseDb.getReference().child("users");
    }

    private void attachSignOutClickListener(){
        mDataBinder.signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AuthUI.getInstance() != null) {
                    AuthUI.getInstance().signOut(UserProfileActivity.this);
                }
            }
        });
    }

    private void attachSubmitClickListener(){
        mDataBinder.userSubmitFormBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertUserData();
            }
        });
    }

    private void insertUserData(){
        //Collect user data
        String firstName = mDataBinder.userFirstNameEditText.getText().toString();
        String lastName = mDataBinder.userLastNameEditText.getText().toString();
        String address = mDataBinder.userAddressEditText.getText().toString();
        String email = mDataBinder.userEmailEditText.getText().toString();
        String phone = mDataBinder.userPhoneEditText.getText().toString();
        int numChildren = Integer.parseInt(mDataBinder.userChildIntEditText.getText().toString());

        //Add the user object to the DB
        User user = new User(firstName, lastName, email, phone, address, numChildren);
        mUserDbReference.push().setValue(user);
    }

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
        attachDbReadListener();
    }

    private void attachDbReadListener(){
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {            }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {            }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {            }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {            }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {            }
            };

            //Attach to Db reference
            mUserDbReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDbReadListener(){
        if(mChildEventListener != null){
            mUserDbReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(UserProfileActivity.this, "Successfully signed in", Toast.LENGTH_SHORT).show();
            } else if (requestCode == RESULT_CANCELED) {
                Toast.makeText(UserProfileActivity.this, "Sign in cancelled", Toast.LENGTH_SHORT).show();
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
        detachDbReadListener();
    }

}
