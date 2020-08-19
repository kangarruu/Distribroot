package com.lefriedman.distribroot.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.SwitchPreferenceCompat;

import com.google.firebase.messaging.FirebaseMessaging;
import com.lefriedman.distribroot.R;

public class DistributorPreferenceFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = DistributorPreferenceFragment.class.getSimpleName();
    private static String mDistributorPrefKey;
    private static String mDistributorPrefTitle;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        Preference preference = findPreference(key);
//        if (preference != null && preference instanceof SwitchPreferenceCompat) {
//            boolean isSubscribed = sharedPreferences.getBoolean(key, false);
//            if (isSubscribed) {
//                FirebaseMessaging.getInstance().subscribeToTopic(key);
//                Log.d(TAG, "onSharedPreferenceChanged: subscribing to updates from: " + key);
//            } else {
//                //unsubscribe
//                FirebaseMessaging.getInstance().unsubscribeFromTopic(key);
//                Log.d(TAG, "onSharedPreferenceChanged: unsubscribing to updates from: " + key);
//            }
//        }

    }

//    public void getSwitchPreference(String id, String title) {
//        mDistributorPrefTitle = title;
//        mDistributorPrefKey = id;
//        generateSwitchPreference(mDistributorPrefKey, mDistributorPrefTitle);
//    }

    public void generateSwitchPreference(String key, String title) {
        PreferenceGroup preferenceGroup = findPreference(getString(R.string.preferences_category_subscribed_key));
        SwitchPreferenceCompat switchPreference = new SwitchPreferenceCompat(getContext());
        switchPreference.setKey(key);
        switchPreference.setTitle(title);
        switchPreference.setChecked(true);
        preferenceGroup.addPreference(switchPreference);
        }
    }


