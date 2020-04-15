package com.gauravnadar.covid19stats;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.gauravnadar.covid19stats.Modals.MyItem2;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class IndiaCovidTracker extends Fragment implements OnMapReadyCallback {

    private static String TAG = "IndiaList";
    List<IndiaListModel> list;
    MapView indiaMap;
    GoogleMap map;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    List<Address> points;
    ProgressBar progress;
    TextView update;
    String date, time;
    ClusterManager<MyItem2> mClusterManager;
    MyCustomRenderer renderer;
    Activity main;



    public IndiaCovidTracker() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_india_covid_tracker, container, false);

        list = new ArrayList<>();
        points = new ArrayList<>();
        indiaMap = (MapView) v.findViewById(R.id.india_map);
        progress = (ProgressBar) v.findViewById(R.id.progress);
        update = (TextView) v.findViewById(R.id.update);
        update.setText("Collecting data please wait");


        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }


        indiaMap.onCreate(mapViewBundle);
        indiaMap.getMapAsync(this);

        Calendar calendar = Calendar.getInstance();

        date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        time = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        NetworkBackground background = new NetworkBackground();
        background.execute();


        return v;


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLngBounds india = new LatLngBounds(new LatLng(23.63936, 68.14712), new LatLng(28.20453, 97.3446));
        int padding = 5;
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(india, padding);
        map.animateCamera(update);


       // mClusterManager = new ClusterManager<MyItem2>(getActivity(), map);
        //map.setOnCameraIdleListener(mClusterManager);
        //map.setOnMarkerClickListener(mClusterManager);

        //MyCustomRenderer renderer = new MyCustomRenderer(getContext(), map, mClusterManager);
        //mClusterManager.setRenderer(renderer);

        map.setInfoWindowAdapter(new MyCustomInfoWindow(getContext()));

        //mClusterManager.getClusterMarkerCollection().setOnInfoWindowAdapter(null);
        //mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new MyCustomInfoWindow(getContext()));



    }

    public void getStateLatLng()
    {



    }


    public class NetworkBackground extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //progress.setVisibility(View.VISIBLE);
            Log.i("value", String.valueOf(10));
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                Document doc = Jsoup.connect("https://www.mohfw.gov.in/").get();
                Log.i(TAG, doc.title());
                // Elements elements = doc.getElementsByClass("data-table table-responsive");
                Elements elements = doc.getElementsByClass("data-table table-responsive");
                elements.html();



                Element table = doc.select("tbody").get(0);
                Elements rows = table.select("tr");

                Log.i("size", String.valueOf(rows.size()));
                for(int a=0; a<rows.size(); a++){

                    Element row = rows.get(a);
                    Elements col = row.select("td");

                    Log.i("full", col.text());

                    if(a == rows.size() || a == rows.size()-1 || a == rows.size()-2 || a==rows.size()-3) {

                    }
                    else{
                        list.add(new IndiaListModel(col.get(1).text(), col.get(2).text(), col.get(3).text(), col.get(4).text()));   //state conf reco deat
                    }

                }

                Log.i(TAG, String.valueOf(list.size()));


            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if(result) {
                Log.i("value", "done");
                //progress.setVisibility(View.INVISIBLE);
                new getStateLatLng().execute();

            }
        }
    }


    public class getStateLatLng extends AsyncTask<Void, Integer, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setProgress(0);
            update.setText("Collecting data 0%");

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progress.setVisibility(View.GONE);
            update.setText("Last updated on "+date+" at "+time);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            progress.setProgress(values[0]);
            update.setText("Collecting data "+String.valueOf(values[0])+"%");
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Geocoder gc = new Geocoder(getContext());
            int size = list.size();
            final int divide = size/4;
            // Log.i("myList", String.valueOf(divide));

            for(int a=0; a<list.size(); a++) {

                //String state = "India, Gujarat";

                try {
                    points = gc.getFromLocationName("India, "+list.get(a).getState(), 1);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.i("myList2", String.valueOf(points.size()));


                if(!points.isEmpty()){
                    Double Lat = points.get(0).getLatitude();
                    Double Lng = points.get(0).getLongitude();

                    final MarkerOptions markerOptions = new MarkerOptions();
                    LatLng latLng = new LatLng(Lat, Lng);
                    markerOptions.position(latLng);
                    markerOptions.title(list.get(a).getState());
                    markerOptions.snippet(list.get(a).getConfirmed()+","+list.get(a).getRecovered()+","+list.get(a).getDeaths());


                   //final MyItem2 myItem2 = new MyItem2(Lat, Lng, list.get(a).state, list.get(a).getConfirmed()+","+list.get(a).getRecovered()+","+list.get(a).getDeaths());



                    main.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                           map.addMarker(markerOptions);
                            //mClusterManager.addItem(myItem2);
                        }
                    });


                    points.clear();

                }
if(a==divide) {
    publishProgress(25);
}
else if(a==divide*2)
{
    publishProgress(50);
}
else if(a==divide*3)
{
    publishProgress(75);
}
else if(a==divide*4)
{
    publishProgress(99);
}

            }


            publishProgress(100);
//            mClusterManager.cluster();
            return true;

        }
    }



    public class MyCustomRenderer extends DefaultClusterRenderer<MyItem2> {
        private static final int MARKER_DIMENSION = 40;
        private static final int MARKER_DIMENSION2 = 80;// 2
        private final IconGenerator iconGenerator;
        private final IconGenerator iconGenerator2;
        private final ImageView markerImageView;
        private final ImageView markerImageView2;

        public MyCustomRenderer(Context context, GoogleMap map, ClusterManager<MyItem2> clusterManager) {
            super(context, map, clusterManager);
            iconGenerator = new IconGenerator(context);
            iconGenerator2 = new IconGenerator(context);// 3
            markerImageView = new ImageView(context);
            markerImageView2 = new ImageView(context);
            markerImageView.setLayoutParams(new ViewGroup.LayoutParams(MARKER_DIMENSION, MARKER_DIMENSION));
            markerImageView2.setLayoutParams(new ViewGroup.LayoutParams(MARKER_DIMENSION2, MARKER_DIMENSION2));
            iconGenerator.setContentView(markerImageView);
            iconGenerator2.setContentView(markerImageView2); // 4
        }

        @Override
        protected void onBeforeClusterItemRendered(MyItem2 item, MarkerOptions markerOptions) { // 5
            markerImageView.setImageResource(R.drawable.alert);  // 6
            Bitmap icon = iconGenerator.makeIcon();  // 7
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));  // 8
            markerOptions.title(item.getTitle());



        }

        @Override
        protected void onBeforeClusterRendered(Cluster<MyItem2> cluster, MarkerOptions markerOptions) {
            markerImageView2.setImageResource(R.drawable.group_alert);
            Bitmap icon = iconGenerator2.makeIcon();  // 7
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }


        @Override
        protected void onClusterRendered(Cluster<MyItem2> cluster, Marker marker) {
            super.onClusterRendered(cluster, marker);

            marker.setTitle(String.valueOf(cluster.getItems().size()));
            marker.setSnippet("Zoom in to view single items");
        }


    }


    public class MyCustomInfoWindow implements GoogleMap.InfoWindowAdapter {


        Context context;

        public MyCustomInfoWindow(Context context) {
            this.context = context;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {


            View view = ((Activity)context).getLayoutInflater().inflate(R.layout.custom_infowindow_india, null);

            TextView state = (TextView) view.findViewById(R.id.state_ind);
            TextView con = (TextView) view.findViewById(R.id.c_ind);
            TextView rec = (TextView) view.findViewById(R.id.r_ind);
            TextView dea = (TextView) view.findViewById(R.id.d_ind);

            String[] data = marker.getSnippet().split(",", 3);

            state.setText(marker.getTitle());
            con.setText(data[0]);
            rec.setText(data[1]);
            dea.setText(data[2]);

            return view;
        }
    }



    @Override
    public void onStart() {
        super.onStart();
        indiaMap.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        indiaMap.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        indiaMap.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        indiaMap.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        indiaMap.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        indiaMap.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        main = (Activity) context;
    }
}

