package com.lefriedman.distribroot.models.retrofit;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Result {

    @SerializedName("formatted_address")
    @Expose
    private String formattedAddress;

    @SerializedName("geometry")
    @Expose
    private Geometry geometry;

    @SerializedName("place_id")
    @Expose
    private String placeId;


    public String getFormattedAddress() {
        return formattedAddress;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public String getPlaceId() {
        return placeId;
    }

    @Override
    public String toString() {
        return "Result{" +
                "formattedAddress='" + formattedAddress + '\'' +
                ", geometry=" + geometry +
                ", placeId='" + placeId + '\'' +
                '}';
    }
}