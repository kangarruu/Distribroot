package com.lefriedman.distribroot.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.lefriedman.distribroot.R;
import com.lefriedman.distribroot.databinding.MarkerBottomSheetBinding;

public class BottomSheetDialogFragment extends com.google.android.material.bottomsheet.BottomSheetDialogFragment {
    private BottomSheetListener mListener;
    private MarkerBottomSheetBinding mDataBiding;
    private String mDistributorName;
    private String mDistributorAddress;

    public BottomSheetDialogFragment(String name, String address) {
        mDistributorName = name;
        mDistributorAddress = address;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.marker_bottom_sheet, container, false);
        mDataBiding = DataBindingUtil.inflate(inflater, R.layout.marker_bottom_sheet, container, false);

        //Set subscribe switch
        mDataBiding.bottomSheetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mListener.onSwitchSelected("Subscribed to distributor");
                } else {
                    mListener.onSwitchSelected("Unsubscribed From distributor");
                }
            }
        });

        //Set title
        mDataBiding.bottomSheetTitleTv.setText(mDistributorName);
        mDataBiding.bottomSheetAddressTv.setText(mDistributorAddress);

        return mDataBiding.getRoot();
    }

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
