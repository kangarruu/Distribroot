package com.lefriedman.distribroot.requests;

import android.location.Location;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
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
    private static GeoFire mGeoFire;
    private GeolocationApi mGeoApi;
    private MutableLiveData<String> toastResponseObserverLiveData = new MutableLiveData<>();
    private static MutableLiveData<MarkerOptions> mDistributorMarkerLiveData;
    private static LatLng mDistributorLatLng;


    public FirebaseClient() {
        mGeoApi = new RetrofitServiceGenerator().getGeolocationApi();
        mGeoFire = new GeoFire(DISTRIBUTOR_LOCATION_REF);
        mDistributorMarkerLiveData = new MutableLiveData<>();
    }

    //Return singleton FirebaseClient
    public static FirebaseClient getInstance() {
        if(sInstance == null){
            sInstance = new FirebaseClient();
        } return sInstance;
    }


    //Make the Geolocation retrofit call and store result in GeoFire
    public LiveData<String> makeRetrofitGeocodeApiCall(String name, String phone, String address, String city, String state, String zip, String apiKey){
        String concatAddress = address + " " + city + " " + state + " " + zip;
        Log.d(TAG, "Making Retrofit call in FirebaseClient makeRetrofitGeocodeApiCall. Address = " + concatAddress);

        //Make a retrofit call using an instance of the GeolocationApi
        Call<ResultWrapper> geoResult = mGeoApi.getGeocode(concatAddress, apiKey);
        Log.d(TAG, "makeRetrofitGeocodeApiCall: APIKEY == " + apiKey);
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

    //Make a GeoQuery using the Location result from the FindDistributionActivity getLastLocation() method
    //postValue() to the MarkerOptions LiveData
    public static void makeGeoQuery(Location result) {
        GeoQuery geoQuery = mGeoFire.queryAtLocation(new GeoLocation(result.getLatitude(), result.getLongitude()),10);
        Log.d(TAG, "makeGeoQuery: Making geoQuery" + geoQuery);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d(TAG, "makeGeoQuery onKeyEntered: making FirebaseQuery for key: " + key );
                DISTRIBUTORS_REF.child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Distributor distributor = snapshot.getValue(Distributor.class);
                        Log.d(TAG, "Firebase ValueEventListener current distributor: " + distributor);

                        //set the distributor LatLang for the map Marker
                        if (location != null) {
                            mDistributorLatLng = new LatLng(location.latitude, location.longitude);
                        }

                        //create the new MarkerOptions and post to LiveData
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(mDistributorLatLng)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                .title(distributor.getName())
                                .snippet(distributor.getAddress());
                        Log.d(TAG, "Firebase ValueEventListener new MarkerOptions being created: " + markerOptions);

                        mDistributorMarkerLiveData.setValue(markerOptions);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "ValueEventListener on Cancelled: error reading Distributor from Database " + error.toException());
                    }
                });
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }

        });
    }

    //Return the MarkerOptions LiveData set in the makeGeoQuery() method
    public static LiveData<MarkerOptions> getDistributorMarkerOptionsLiveData() {
        Log.d(TAG, "getDistributorMarkerOptionsLiveData: returning mDistributorMarkerLiveData: " + mDistributorMarkerLiveData.getValue());
        return mDistributorMarkerLiveData;
    }

}
