package com.lefriedman.distribroot.models.retrofit;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ResultWrapper {

    @SerializedName("results")
    @Expose
    private List<Result> results = null;
    @SerializedName("status")
    @Expose
    private String status;

    public List<Result> getResults() {
        return results;
    }

    public String getStatus() {
        return status;
    }


    @Override
    public String toString() {
        return "ResultWrapper{" +
                "results=" + results +
                ", status='" + status + '\'' +
                '}';
    }
}