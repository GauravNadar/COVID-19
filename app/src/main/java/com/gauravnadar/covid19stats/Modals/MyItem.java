package com.gauravnadar.covid19stats.Modals;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyItem implements ClusterItem {
    LatLng mPosition;
     String mTitle;
     String mSnippet;
     String mConfirmed;

     public MyItem()
     {

     }

    public MyItem(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    public MyItem(double lat, double lng, String title, String snippet) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getSnippet() {
        return mSnippet;
    }

    public void setmPosition(LatLng mPosition) {
        this.mPosition = mPosition;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public void setmSnippet(String mSnippet) {
        this.mSnippet = mSnippet;
    }

    public String getmConfirmed() {
        return mConfirmed;
    }

    public void setmConfirmed(String mConfirmed) {
        this.mConfirmed = mConfirmed;
    }
}