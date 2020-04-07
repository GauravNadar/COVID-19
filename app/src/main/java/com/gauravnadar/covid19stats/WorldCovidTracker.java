package com.gauravnadar.covid19stats;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gauravnadar.covid19stats.Modals.DailyReportsModel;
import com.gauravnadar.covid19stats.Modals.MyItem;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;


/**
 * A simple {@link Fragment} subclass.
 */
public class WorldCovidTracker extends Fragment implements OnMapReadyCallback {

Activity main;
    MapView world_map;
    GoogleMap Wmap;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";
    ProgressBar progress;
    Worker worker;

    FileOutputStream out = null;
    FileOutputStream out2 = null;
    FileOutputStream out3 = null;
    FileOutputStream Dailyout = null;

    Handler handler;
    String date2, lastUpdatedOn;
    private TextView update;
List<DailyReportsModel> dailList;
    ClusterManager<MyItem> mClusterManager;
    MyCustomRenderer renderer;

    public WorldCovidTracker() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_world_covid_tracker, container, false);

        world_map = (MapView) v.findViewById(R.id.world_map);
        progress = (ProgressBar) v.findViewById(R.id.progressBar);
        update = (TextView) v.findViewById(R.id.update2);
        dailList = new ArrayList<>();
        update.setText("Loading data please wait");


        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }


        world_map.onCreate(mapViewBundle);
        world_map.getMapAsync(this);





        DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        date2 = df.format(cal.getTime());

        handler = new Handler(Looper.getMainLooper()) {

            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);

                Log.e("zzzzzzz", (String) msg.obj);

            }

            ;

        };



        new collectDataFirstLoad().execute();



        return v;
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        Wmap = googleMap;
        mClusterManager = new ClusterManager<MyItem>(getActivity(), Wmap);
        Wmap.setOnCameraIdleListener(mClusterManager);
        Wmap.setOnMarkerClickListener(mClusterManager);

        renderer = new MyCustomRenderer(getActivity(), Wmap, mClusterManager);
        mClusterManager.setRenderer(renderer);


        MyCustomInfoWindow window =  new MyCustomInfoWindow(getContext());
        Wmap.setInfoWindowAdapter(mClusterManager.getMarkerManager());

        mClusterManager.getClusterMarkerCollection().setOnInfoWindowAdapter(null);
        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new MyCustomInfoWindow(getContext()));

      /*  mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {

                return false;
            }
        });*/
    }



    public class collectDataFirstLoad extends AsyncTask<Void, Integer, Boolean>
    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress.setProgress(0);
            update.setText("Loading data 0%");
            worker = new Worker();
            Message message =  Message.obtain();
            Log.i("onPre", "0");
        }


        @Override
        protected Boolean doInBackground(Void... voids) {

            Log.i("onBack", "1");


            try {

                // FileInputStream input = new FileInputStream("stats.csv");
                CSVReader reader = new CSVReader(new FileReader("data/data/com.gauravnadar.covid19stats/files/daily.csv"));
                String[] nextLine;

                while ((nextLine = reader.readNext()) != null)
 {
     int last_row = nextLine.length-1;



     int divide = last_row/4;


                    DailyReportsModel data = new DailyReportsModel(nextLine[2], nextLine[3], nextLine[4], nextLine[5], nextLine[6], nextLine[7], nextLine[8], nextLine[9], nextLine[10]);
                    if (nextLine[3].equals("Country_Region")) {


                        lastUpdatedOn = nextLine[last_row];
                    } else {
                        dailList.add(data);
                    }




                }
                Log.d("sixe", String.valueOf(dailList.size()));


                //loadMap();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }





            return true;

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            Log.i("onProgree", "2");
            progress.setProgress(values[0]);
            update.setText("Loading Data 52%");
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            Log.i("onPost", "3");
            progress.setProgress(100);
            progress.setVisibility(View.GONE);
           // update.setText("Last updated on "+date2);

new PlotMarkers().execute();

        }


    }



    public class PlotMarkers extends AsyncTask<Void, Integer, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progress.setVisibility(View.VISIBLE);
            progress.setProgress(0);
            update.setText("Plotting markers");
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            progress.setProgress(75);
            progress.setVisibility(View.GONE);
            update.setText("Last updated on "+date2);
            FloatingActionButton fab = main.findViewById(R.id.fab);
            fab.setImageResource(R.drawable.refresh);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {


            Log.e("timing", "loadMap");

           // final IconGenerator icon = new IconGenerator(getActivity());
            String rColour = "";
            LatLng latLng = null;
            int fixed = 200000;





            DailyReportsModel model = new DailyReportsModel();

            for (int i = 1; i < dailList.size(); i++) {



                if(!dailList.get(i).getLatitude().isEmpty() && !dailList.get(i).getLongitude().isEmpty()) {
                    final MyItem offsetItem = new MyItem(Double.valueOf(dailList.get(i).getLatitude()), Double.valueOf(dailList.get(i).getLongitude()), dailList.get(i).getCountry()+","+dailList.get(i).getProvince(), dailList.get(i).getConfirmed()+","+dailList.get(i).getDeaths()+","+dailList.get(i).getRecovered());



main.runOnUiThread(new Runnable() {
    @Override
    public void run() {

        mClusterManager.addItem(offsetItem);


    }
});
                   // mClusterManager.addItem(offsetItem);

                }



            }

          main.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mClusterManager.cluster();
                }
            });

            //mClusterManager.cluster();

            return true;
        }
    }


    public class MyCustomRenderer extends DefaultClusterRenderer<MyItem> {
        private static final int MARKER_DIMENSION = 40;
        private static final int MARKER_DIMENSION2 = 80;// 2
        private final IconGenerator iconGenerator;
        private final IconGenerator iconGenerator2;
        private final ImageView markerImageView;
        private final ImageView markerImageView2;

        public MyCustomRenderer(Context context, GoogleMap map, ClusterManager<MyItem> clusterManager) {
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
        protected void onBeforeClusterItemRendered(MyItem item, MarkerOptions markerOptions) { // 5
            markerImageView.setImageResource(R.drawable.alert);  // 6
            Bitmap icon = iconGenerator.makeIcon();  // 7
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));  // 8
            markerOptions.title(item.getTitle());



        }

        @Override
        protected void onBeforeClusterRendered(Cluster<MyItem> cluster, MarkerOptions markerOptions) {
            markerImageView2.setImageResource(R.drawable.group_alert);
            Bitmap icon = iconGenerator2.makeIcon();  // 7
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        }


        @Override
        protected void onClusterRendered(Cluster<MyItem> cluster, Marker marker) {
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



            View view = ((Activity)context).getLayoutInflater().inflate(R.layout.single_map_marker, null);
            TextView c = (TextView) view.findViewById(R.id.c);
            TextView d = (TextView) view.findViewById(R.id.d);
            TextView r = (TextView) view.findViewById(R.id.r);
            TextView n = (TextView) view.findViewById(R.id.name);
            TextView p = (TextView) view.findViewById(R.id.province);

String[] data = marker.getTitle().split(",",2);
            String[] data2 = marker.getSnippet().split(",",3);

            n.setText(data[0]);
            p.setText(data[1]);

            c.setText(data2[0]);
            d.setText(data2[1]);
            r.setText(data2[2]);

            return view;
        }
    }



    @Override
    public void onStart() {
        super.onStart();
        world_map.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        world_map.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        world_map.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        world_map.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        world_map.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        world_map.onLowMemory();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        main = (Activity) context;

    }
}
