package com.lefriedman.distribroot.util;

import android.widget.EditText;

import androidx.databinding.BindingAdapter;

import com.google.android.material.textfield.TextInputLayout;
import com.lefriedman.distribroot.R;

public class DataBindingAdapter {

    @BindingAdapter("app:errorText")
    public static void setErrorMessage(TextInputLayout view, String string){
        EditText editText = view.getEditText();
            if (editText.isFocused()){
                if(string == null || string.isEmpty()){
                    view.setError(view.getResources().getString(R.string.validation_error_empty));
                } else {
                    view.setError(null);
                }

            }
        }

}
