package com.lefriedman.distribroot.ui;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.lefriedman.distribroot.R;

public abstract class BaseActivity extends AppCompatActivity {

    private ProgressBar progressBar;

    @Override
    public void setContentView(int layoutResID) {
        ConstraintLayout constraintLayout = (ConstraintLayout) getLayoutInflater().inflate(R.layout.activity_base, null);
        FrameLayout activityLayoutHolder = constraintLayout.findViewById(R.id.activity_layout_holder);

        getLayoutInflater().inflate(layoutResID, activityLayoutHolder, true);

        super.setContentView(layoutResID);
    }

    //public setter for progressBar
    public void ShowProgressBar(boolean visibility){
        progressBar.setVisibility(visibility ? View.VISIBLE : View.INVISIBLE);
    }
}
