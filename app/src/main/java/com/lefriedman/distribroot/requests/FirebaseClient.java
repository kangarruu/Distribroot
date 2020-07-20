package com.lefriedman.distribroot.requests;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lefriedman.distribroot.livedata.DataSnapshotLiveData;
import com.lefriedman.distribroot.models.Distributor;
import com.lefriedman.distribroot.models.retrofit.Result;
import com.lefriedman.distribroot.models.retrofit.ResultWrapper;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.lefriedman.distribroot.util.Constants.DISTRIBUTORS_REF;
import static com.lefriedman.distribroot.util.Constants.DISTRIBUTOR_LOCATION_REF;


public class FirebaseClient {
    private static final String TAG = FirebaseClient.class.getSimpleName();

    private static FirebaseClient sInstance;
    private GeolocationApi mGeoApi;
    private MutableLiveData<String> toastResponseObserverLiveData = new MutableLiveData<>();
    private GeoFire mGeoFire;
    private DataSnapshotLiveData mDatasnapshotLiveData;

    public FirebaseClient() {
        mGeoApi = new RetrofitServiceGenerator().getGeolocationApi();
        mGeoFire = new GeoFire(DISTRIBUTOR_LOCATION_REF);
    }

    //Return singleton FirebaseClient
    public static FirebaseClient getInstance() {
        if(sInstance == null){
            sInstance = new FirebaseClient();
        } return sInstance;
    }

    public LiveData<DataSnapshot> getDataSnapshotLiveData(String key) {
        mDatasnapshotLiveData = new DataSnapshotLiveData(DISTRIBUTORS_REF.child(key));
        Log.d(TAG, "FirebaseClient getDataSnapshotLiveData: returning dataSnapshot LiveData at key: " + key + " datasnapshot: " + mDatasnapshotLiveData.getValue());

        return mDatasnapshotLiveData;
    }

    //Make the Geolocation retrofit call and store result in GeoFire
    public LiveData<String> makeRetrofitGeocodeApiCall(String name, String phone, String address, String city, String state, String zip, String apiKey){
        String concatAddress = address + " " + city + " " + state + " " + zip;
        Log.d(TAG, "Making Retrofit call in FirebaseClient makeRetrofitGeocodeApiCall. Address = " + concatAddress);

        //Make a retrofit call using an instance of the GeolocationApi
        Call<ResultWrapper> geoResult = mGeoApi.getGeocode(concatAddress, apiKey);
        geoResult.enqueue(new Callback<ResultWrapper>() {
            @Override
            public void onResponse(Call<ResultWrapper> call, Response<ResultWrapper> response) {
                switch (response.body().getStatus()){
                    case "OK":
                        List<Result> resultList = response.body().getResults();
                        Log.d(TAG, "onResponse: resultList: " + response.body().getResults());
                        String placeId = resultList.get(0).getPlaceId();
                        Double lat = resultList.get(0).getGeometry().getLocation().getLat();
                        Double lng =  resultList.get(0).getGeometry().getLocation().getLng();

                        //Create new distributor object to add to the DB
                        Distributor distributor = new Distributor(name, phone, address, city, state, zip, placeId);
                        //Generate a pushId in Firebase Database for use in GeoFire
                        String pushId = DISTRIBUTORS_REF.push().getKey();
                        //Set the distributor & GeoFire data using the new key
                        DISTRIBUTORS_REF.child(pushId).setValue(distributor);
                        mGeoFire.setLocation(pushId, new GeoLocation(lat, lng), new GeoFire.CompletionListener() {
                            @Override
                            public void onComplete(String key, DatabaseError error) {
                                if (error !=null){
                                    Log.d(TAG, "GeoFire onComplete: There was an error adding distributor geoCoordinates to GeoFire ");
                                }else {
                                    Log.d(TAG, "GeoFire onComplete: new distributor geoCoordinates saved to GeoFire successfully");
                                    toastResponseObserverLiveData.postValue("New distributor saved successfully");
                                }
                            }
                        });
                        break;
                    case "INVALID_REQUEST":
                        toastResponseObserverLiveData.postValue("Invalid request, please check address components");
                        break;
                    case "ZERO_RESULTS":
                        toastResponseObserverLiveData.postValue("No results, please check address components");
                        break;
                    case "OVER_QUERY_LIMIT":
                        Log.d(TAG, "GeoResult onResponse: OVER_QUERY_LIMIT");
                        break;
                    case "UNKNOWN_ERROR":
                        toastResponseObserverLiveData.postValue("An unknown error has occurred, please try again later");
                        break;
                    default:
                        Log.d(TAG, "GeoResult onResponse: " + response.body().getStatus());

                }
            }

            @Override
            public void onFailure(Call<ResultWrapper> call, Throwable t) {
                Log.d(TAG, "FirebaseClient makeRetrofitGeocodeApiCall() onFailure: Retrofit call failed: " + t);
            }
        });
        return toastResponseObserverLiveData;
    }



}
