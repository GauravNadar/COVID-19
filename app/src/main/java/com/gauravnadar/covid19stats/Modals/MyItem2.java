package com.gauravnadar.covid19stats.Modals;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class MyItem2 implements ClusterItem {

    String state, confirmed, recovered, death;
    LatLng mPosition;
    String mTitle;
    String mSnippet;

    public MyItem2() {
    }

    public MyItem2(double lat, double lng, String title, String snippet) {
        mPosition = new LatLng(lat, lng);
        mTitle = title;
        mSnippet = snippet;

    }

    public MyItem2(String state, String confirmed, String recovered, String death) {
        this.state = state;
        this.confirmed = confirmed;
        this.recovered = recovered;
        this.death = death;
    }


    @Override
    public LatLng getPosition() {
        return null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(String confirmed) {
        this.confirmed = confirmed;
    }

    public String getRecovered() {
        return recovered;
    }

    public void setRecovered(String recovered) {
        this.recovered = recovered;
    }

    public String getDeath() {
        return death;
    }

    public void setDeath(String death) {
        this.death = death;
    }
}
