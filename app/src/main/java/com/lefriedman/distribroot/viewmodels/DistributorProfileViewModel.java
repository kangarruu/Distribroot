package com.lefriedman.distribroot.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.lefriedman.distribroot.R;
import com.lefriedman.distribroot.liveData.Event;
import com.lefriedman.distribroot.repositories.DistributionRepository;

public class DistributorProfileViewModel extends AndroidViewModel {
    private static final String TAG = DistributorProfileViewModel.class.getSimpleName();

    private DistributionRepository mRepository;
    private String apiKey;
    private String finalName, finalPhone, finalAddress, finalCity, finalState, finalZip;

    public MutableLiveData<String> name = new MutableLiveData();
    public MutableLiveData<String> phone = new MutableLiveData();
    public MutableLiveData<String> address = new MutableLiveData();
    public MutableLiveData<String> city = new MutableLiveData();
    public MutableLiveData<String> state = new MutableLiveData();
    public MutableLiveData<String> zip = new MutableLiveData();

    private MutableLiveData<Boolean> formValid = new MutableLiveData();
    public MediatorLiveData<Boolean> formIsValid = new MediatorLiveData<>();

    public DistributorProfileViewModel(@NonNull Application application) {
        super(application);
        apiKey = getApplication().getString(R.string.maps_api_key);
        mRepository = DistributionRepository.getInstance();

        formIsValid.addSource(name, value -> formIsValid.setValue(validateForm().getValue()));
        formIsValid.addSource(phone, value -> formIsValid.setValue(validateForm().getValue()));
        formIsValid.addSource(address, value -> formIsValid.setValue(validateForm().getValue()));
        formIsValid.addSource(city, value -> formIsValid.setValue(validateForm().getValue()));
        formIsValid.addSource(state, value -> formIsValid.setValue(validateForm().getValue()));
        formIsValid.addSource(zip, value -> formIsValid.setValue(validateForm().getValue()));
    }

    //Helper method to validate that none of the form fields are empty
    //@Return a Boolean LiveData that is being set as the value of the MediatorLiveData formIsValid
    //formIsValid is databound to android:enabled attribute of the submit button
      private LiveData<Boolean> validateForm() {
        try{
            Boolean nameIsValid = !name.getValue().isEmpty() && name.getValue() != null;
            Boolean phoneIsValid = !phone.getValue().isEmpty() && phone.getValue() != null;
            Boolean addressIsValid = !address.getValue().isEmpty() && address.getValue() != null;
            Boolean cityIsValid = !city.getValue().isEmpty() && city.getValue() != null;
            Boolean stateIsValid = !state.getValue().isEmpty() && state.getValue() != null;
            Boolean zipIsValid = !zip.getValue().isEmpty() && zip.getValue() != null;
            formValid.setValue(nameIsValid && phoneIsValid && addressIsValid
                    && cityIsValid && stateIsValid && zipIsValid);
        } catch (Exception e) {
            e.printStackTrace();
        }
          return formValid;
    }

    //Once form is validated and submit button is enabled
    //Send the user entered values to the FireBaseClient via the repository
    //@return a liveData with an error or success message
    public LiveData<Event<String>> onSubmitClick() {
        try {
            finalName = name.getValue().trim();
            finalPhone = phone.getValue().trim();
            finalAddress = address.getValue().trim();
            finalCity = city.getValue().trim();
            finalState = state.getValue().trim();
            finalZip = zip.getValue().trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mRepository.makeRetrofitGeocodeApiCall(finalName, finalPhone, finalAddress, finalCity, finalState, finalZip, apiKey);
    }





}
