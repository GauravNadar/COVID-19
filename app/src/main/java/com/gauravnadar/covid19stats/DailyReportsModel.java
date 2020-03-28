package com.gauravnadar.covid19stats;

import android.content.res.Configuration;

public class DailyReportsModel {

    String FIPS, admin2, province, country, lastupdate, confirmed, deaths, recovered;
    String latitude, longitude, active, combined_key;

    public DailyReportsModel(String province, String country, String lastupdate , String latitude, String longitude , String confirmed, String deaths, String recovered, String active) {
        //this.FIPS = FIPS;
        //this.admin2 = admin2;
        this.province = province;
        this.country = country;
        this.lastupdate = lastupdate;
        this.confirmed = confirmed;
        this.deaths = deaths;
        this.recovered = recovered;
        this.latitude = latitude;
        this.longitude = longitude;
        this.active = active;
        //this.combined_key = combined_key;
    }

    public DailyReportsModel() {
    }

    public String getFIPS() {
        return FIPS;
    }

    public void setFIPS(String FIPS) {
        this.FIPS = FIPS;
    }

    public String getAdmin2() {
        return admin2;
    }

    public void setAdmin2(String admin2) {
        this.admin2 = admin2;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getCombined_key() {
        return combined_key;
    }

    public void setCombined_key(String combined_key) {
        this.combined_key = combined_key;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(String lastupdate) {
        this.lastupdate = lastupdate;
    }

    public String getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(String confirmed) {
        this.confirmed = confirmed;
    }

    public String getDeaths() {
        return deaths;
    }

    public void setDeaths(String deaths) {
        this.deaths = deaths;
    }

    public String getRecovered() {
        return recovered;
    }

    public void setRecovered(String recovered) {
        this.recovered = recovered;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

}
