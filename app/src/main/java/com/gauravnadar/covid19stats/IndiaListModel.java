package com.gauravnadar.covid19stats;

public class IndiaListModel {

    String state, confirmed, recovered, deaths;

    public IndiaListModel(String state, String confirmed, String recovered, String deaths) {
        this.state = state;
        this.confirmed = confirmed;
        this.recovered = recovered;
        this.deaths = deaths;
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

    public String getDeaths() {
        return deaths;
    }

    public void setDeaths(String deaths) {
        this.deaths = deaths;
    }
}
