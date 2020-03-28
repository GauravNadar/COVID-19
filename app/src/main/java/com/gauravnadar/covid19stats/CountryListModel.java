package com.gauravnadar.covid19stats;

public class CountryListModel {

    String Country;
    String Province;
    String Confirmed;

    public CountryListModel(String country, String province, String confirmed) {
        Country = country;
        Province = province;
        Confirmed = confirmed;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public String getProvince() {
        return Province;
    }

    public void setProvince(String province) {
        Province = province;
    }

    public String getConfirmed() {
        return Confirmed;
    }

    public void setConfirmed(String confirmed) {
        Confirmed = confirmed;
    }
}

