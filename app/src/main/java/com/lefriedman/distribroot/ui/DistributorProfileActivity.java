package com.lefriedman.distribroot.ui;

import androidx.databinding.DataBindingUtil;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.lefriedman.distribroot.R;
import com.lefriedman.distribroot.databinding.ActivityDistributorProfileBinding;
import com.lefriedman.distribroot.liveData.Event;
import com.lefriedman.distribroot.viewmodels.DistributorProfileViewModel;

import static com.lefriedman.distribroot.requests.FirebaseClient.distributionSuccessful;


public class DistributorProfileActivity extends BaseActivity {

    private static final String TAG = DistributorProfileActivity.class.getSimpleName();

    private ActivityDistributorProfileBinding mDataBinder;
    private DistributorProfileViewModel mViewmodel;
    private LiveData<Event<String>> toastMessageLiveData = new MutableLiveData<>();
    private String mToastMessage = "";
    private Toast submitResponseToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_distributor_profile);

        //Set up ViewModel
        mViewmodel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory
                .getInstance(this.getApplication())).get(DistributorProfileViewModel.class);

        //Databinding. Specify current activity as lifecycle owner
        mDataBinder = DataBindingUtil.setContentView(this, R.layout.activity_distributor_profile);
        mDataBinder.setLifecycleOwner(this);
        mDataBinder.setViewModel(mViewmodel);

        //Collect user input onClick and return response message to submission
        //LiveData wrapped in EventWrapper to prevent LiveData callback from displaying previous
        //error response due to LiveData callback
        mDataBinder.userSubmitFormBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if (toastMessageLiveData != null){
                        toastMessageLiveData.removeObservers(DistributorProfileActivity.this);
                    }
                    toastMessageLiveData = mViewmodel.onSubmitClick();
                    toastMessageLiveData.observe(DistributorProfileActivity.this, new Observer<Event<String>>() {
                        @Override
                        public void onChanged(Event<String> stringEvent) {
                            Log.d(TAG, "onChanged: getToastResponseMessage ==  " + stringEvent);
                            submitResponseToast.makeText(DistributorProfileActivity.this, stringEvent.getContentIfNotHandled(), Toast.LENGTH_SHORT).show();

                            if (distributionSuccessful) {
                                Intent intent = new Intent(DistributorProfileActivity.this,  MainActivity.class);
                                startActivity(intent);
                            }

                        }

                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}

