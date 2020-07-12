package com.lefriedman.distribroot.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Distributor implements Parcelable {

    private String name;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zip;
    private String placeId;

    public Distributor(String name, String phone, String address, String city, String state, String zip, String placeId) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.state = state;
        this.zip = zip;
        this.placeId = placeId;
    }

    public Distributor() {
    }


    protected Distributor(Parcel in) {
        name = in.readString();
        phone = in.readString();
        address = in.readString();
        city = in.readString();
        state = in.readString();
        zip = in.readString();
        placeId = in.readString();
    }

    public static final Creator<Distributor> CREATOR = new Creator<Distributor>() {
        @Override
        public Distributor createFromParcel(Parcel in) {
            return new Distributor(in);
        }

        @Override
        public Distributor[] newArray(int size) {
            return new Distributor[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(name);
        dest.writeString(phone);
        dest.writeString(address);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(zip);
        dest.writeString(placeId);
    }


    @Override
    public String toString() {
        return "Distributor{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", zip='" + zip + '\'' +
                ", placeId='" + placeId + '\'' +
                '}';
    }
}
