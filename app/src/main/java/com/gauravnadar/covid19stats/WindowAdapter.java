package com.gauravnadar.covid19stats;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;

public class WindowAdapter implements GoogleMap.InfoWindowAdapter {

    Context context;
    ArrayList<DailyReportsModel> list;

    public WindowAdapter(Context context) {
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.single_country_stat_item, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;

    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }
}
