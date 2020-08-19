package com.lefriedman.distribroot.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.lefriedman.distribroot.R;
import com.lefriedman.distribroot.databinding.MarkerBottomSheetBinding;
import com.lefriedman.distribroot.services.WidgetService;

public class BottomSheetDialogFragment extends com.google.android.material.bottomsheet.BottomSheetDialogFragment {
    private static final String TAG = BottomSheetDialogFragment.class.getSimpleName();

    private BottomSheetListener mListener;
    private MarkerBottomSheetBinding mDataBiding;
    private String mDistributorName;
    private String mDistributorAddress;
    private String mDistributorId;
    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor prefEditor;

    //get the details of the marker that opened the bottomSheet
    public BottomSheetDialogFragment(String name, String address, String id) {
        mDistributorName = name;
        mDistributorAddress = address;
        mDistributorId = id;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        prefEditor = sharedPrefs.edit();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDataBiding = DataBindingUtil.inflate(inflater, R.layout.marker_bottom_sheet, container, false);

        //set the Switch position to value from sharedPrefs
        mDataBiding.bottomSheetSwitch.setChecked(sharedPrefs.getBoolean(mDistributorId, false));

        //Set listener to listen for switch changes and update sharedPrefs
        mDataBiding.bottomSheetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //Update sharedPrefs and alert the user that change was handled
                    updateSwitchState(isChecked);
                    mListener.onSwitchSelected(getString(R.string.bottom_sheet_subscribed_toast_text) + " " + mDistributorName);
                } else {
                    updateSwitchState(isChecked);
                    mListener.onSwitchSelected(getString(R.string.bottom_sheet_unsubscribed_toast_text) + " " + mDistributorName);
                }
            }
        });

        //Set title and address
        mDataBiding.bottomSheetTitleTv.setText(mDistributorName);
        mDataBiding.bottomSheetAddressTv.setText(mDistributorAddress);

        return mDataBiding.getRoot();
    }


    //Update the state of the switch in sharedPrefs
    // update widget and subscribe to topic in FirebaseMessaging
    private void updateSwitchState(Boolean isChecked) {
        prefEditor.putBoolean(mDistributorId, isChecked);
        prefEditor.commit();

        String topic = "key_" + mDistributorId.replace(" ", "_").toLowerCase();

        if (isChecked) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d(TAG, "onComplete: subscribed to " + topic + " successfully");

                    if (!task.isSuccessful()){
                        Log.d(TAG, "onComplete: Subscription to " + topic + " failed");
                    }
                }
            });
        } else {
            //unsubscribe
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Log.d(TAG, "onComplete: unsubscribed from " + topic + " successfully");

                    if (!task.isSuccessful()){
                        Log.d(TAG, "onComplete: failed to  unsubscribe from " + topic);
                    }
                }
            });
        }
    }

    //interface for handling callbacks to activity
    public interface BottomSheetListener {
        void onSwitchSelected(String text);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            mListener = (BottomSheetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " forgot to implements BottomSheetListener");
        }
    }
}
