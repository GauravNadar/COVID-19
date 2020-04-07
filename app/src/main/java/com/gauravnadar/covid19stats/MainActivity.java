package com.gauravnadar.covid19stats;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.gauravnadar.covid19stats.Modals.DailyReportsModel;
import com.gauravnadar.covid19stats.Modals.MyItem;
import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.Display;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.os.Message;
import android.os.PersistableBundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.opencsv.CSVReader;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {


    ArrayList<DailyReportsModel> dailList;
    GoogleMap map;
    MapView mapView;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    ProgressDialog progress;

    Boolean markerShow = false;
    Marker mk = null;

    Boolean MapSet = false;
    TextView usage;

    ClusterManager<MyItem> mClusterManager;

    AppUpdater appUpdater;
    String lastUpdatedOn;
    FloatingActionButton fab;




    AlertDialog.Builder alert;
    AlertDialog dialog;

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    BackgroundTask task;
    FrameLayout container;

    FileOutputStream out = null;
    FileOutputStream out2 = null;
    FileOutputStream out3 = null;
    FileOutputStream Dailyout = null;

    String date2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progress = new ProgressDialog(MainActivity.this);
        progress.setMessage("This may take up few seconds depending on your Internet speed");
        progress.setTitle("Loading Global Data");
        progress.setCanceledOnTouchOutside(false);

        prefs = MainActivity.this.getPreferences(MODE_PRIVATE);
        prefs.getBoolean("firstStart", true);







        alert = new AlertDialog.Builder(this);
        alert.setCancelable(false);
        alert.setMessage("You are not connected to Network, Please oonnect to Internet and Open the Application");
        alert.setTitle("No Network Found");
        alert.setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                finish();
            }
        });
        dialog = alert.create();


        appUpdater = new AppUpdater(this)
                .setGitHubUserAndRepo("GauravNadar", "COVID-19")
                .setDisplay(Display.DIALOG)
                .setUpdateFrom(UpdateFrom.XML)
                .setTitleOnUpdateAvailable("Update available")
                .setContentOnUpdateAvailable("Please update the App to Continue")
                .setTitleOnUpdateNotAvailable("Update not available")
                .setContentOnUpdateNotAvailable("No update available. Check for updates again later!")
                .setButtonUpdate("Update")
                .setButtonDismissClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setButtonDoNotShowAgainClickListener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                // Notification icon
                .setCancelable(false) // Dialog could not be dismissable
                .setUpdateXML("https://raw.githubusercontent.com/GauravNadar/COVID-19/master/update.xml")
                .showAppUpdated(false);



        Boolean signal = isNetworkConnected();
        if(!signal)
        {
            dialog.show();
            //appUpdater.start();
        }
        else{

            appUpdater.start();

            if(prefs.getBoolean("firstStart", true))
            {
                Log.e("background", "called");
                task = new BackgroundTask();
                task.execute();
            }

           // getSupportFragmentManager().beginTransaction().replace(R.id.container, new WorldCovidTracker()).commit();
        }

        //appUpdater.start();



        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        fab = findViewById(R.id.fab);
        fab.setImageResource(R.drawable.wait);
        usage = (TextView) findViewById(R.id.usage);
        container = (FrameLayout) findViewById(R.id.container);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();*/

                if (!MapSet) {
                    getDailyReports();
                    loadMap();
                    //usage.setVisibility(View.VISIBLE);
                    MapSet = true;
                } else {

                    Toast.makeText(MainActivity.this, "All Locations Already Tracked", Toast.LENGTH_LONG).show();
                }
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


/*

        if(prefs.getBoolean("firstStart", true))
        {
            Log.e("background", "called");
            task = new BackgroundTask();
            task.execute();
        }
*/




       getSupportFragmentManager().beginTransaction().replace(R.id.container, new WorldCovidTracker()).commit();







        // View bottomsheet = findViewById(R.id.nested);

        mapView = findViewById(R.id.map);
        // bottomSheetBehavior = BottomSheetBehavior.from(bottomsheet);


        dailList = new ArrayList<>();



        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }


        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        getDailyReports();



        // getData();

        //######################################################################################


    }

    private void startJob() {


        ComponentName componentName = new ComponentName(this, Scheduler.class);
        JobInfo info = new JobInfo.Builder(111, componentName)
                .setPeriodic(15 * 60 * 1000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();


        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resutCode = scheduler.schedule(info);


        if (resutCode == JobScheduler.RESULT_SUCCESS) {
            Log.d("Job", "Result Success");

        } else {
            Log.d("Job", "Result Failed");
        }


    }





    private void getDailyReports() {


        loadDailyReports();


    }

    private void loadDailyReports() {

        try {

            // FileInputStream input = new FileInputStream("stats.csv");
            CSVReader reader = new CSVReader(new FileReader("data/data/com.gauravnadar.covid19stats/files/daily.csv"));
            String[] nextLine;

            while ((nextLine = reader.readNext()) != null) {


                DailyReportsModel data = new DailyReportsModel(nextLine[2], nextLine[3], nextLine[4], nextLine[5], nextLine[6], nextLine[7], nextLine[8], nextLine[9], nextLine[10]);
                if (nextLine[3].equals("Country_Region")) {

                    int last_row = nextLine.length-1;
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


    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

        Bundle mapViewBundle = outState.getBundle(MAP_VIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.e("timing", "onMapReady");
        map = googleMap;


        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if (markerShow) {
                    mk.remove();
                }

            }
        });

        final IconGenerator icon = new IconGenerator(this);

        map.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
            @Override
            public void onCircleClick(Circle circle) {


                if (markerShow) {
                    //mk.setVisible(false);
                    mk.remove();
                }

                String co = null, d = null, r = null, n = null, p = null;

                DailyReportsModel model1 = new DailyReportsModel();

                for (DailyReportsModel model : dailList) {
                    if (model.getLongitude().equals(String.valueOf(circle.getCenter().longitude)) && model.getLatitude().equals(String.valueOf(circle.getCenter().latitude))) {
                        co = model.getConfirmed();
                        d = model.getDeaths();
                        r = model.getRecovered();
                        n = model.getCountry();
                        p = model.getProvince();

                    }
                }

                mk = map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(MainActivity.this, R.drawable.amu_bubble_shadow, co, d, r, n, p)))
                        .position(circle.getCenter())
                        .anchor(icon.getAnchorU(), icon.getAnchorV())
                );


                map.moveCamera(CameraUpdateFactory.newLatLng(circle.getCenter()));

                markerShow = true;


            }
        });


        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                Toast.makeText(MainActivity.this, marker.getPosition().toString(), Toast.LENGTH_LONG).show();
                return true;
            }
        });


    }


    public void loadMap() {

        Log.e("timing", "loadMap");

        final IconGenerator icon = new IconGenerator(this);
        String rColour = "";
        LatLng latLng = null;
        int fixed = 200000;

        mClusterManager = new ClusterManager<MyItem>(this, map);
        map.setOnCameraIdleListener(mClusterManager);
        map.setOnMarkerClickListener(mClusterManager);

        MyCustomRenderer renderer = new MyCustomRenderer(MainActivity.this, map, mClusterManager);


        mClusterManager.setRenderer(renderer);
        DailyReportsModel model = new DailyReportsModel();

        for (int i = 1; i < dailList.size(); i++) {


            // latLng = new LatLng(Double.valueOf(dailList.get(i).getLatitude()),  Double.valueOf(dailList.get(i).getLongitude()));
               /* Circle c = map.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(fixed)
                        //.getStrokeWidth()
                        .fillColor(0x88AB1006)  //fillColor(0x220000FF)
                        .clickable(true)
                        .strokeWidth(2.0f)

                );*/

if(!dailList.get(i).getLatitude().isEmpty() && !dailList.get(i).getLongitude().isEmpty()) {
    MyItem offsetItem = new MyItem(Double.valueOf(dailList.get(i).getLatitude()), Double.valueOf(dailList.get(i).getLongitude()));


    mClusterManager.addItem(offsetItem);

}

           /* if(Integer.valueOf(dailList.get(i).getConfirmed()) <100 )
            {
                rad=100000;  //100000
                rColour = "03AB1A";  //green

            }
            else if(Integer.valueOf(dailList.get(i).getConfirmed()) >100 && Integer.valueOf(dailList.get(i).getConfirmed()) >1000) {
                rad = 150000;
                rColour = "00A5AB";  //bluish

            }
            else if (Integer.valueOf(dailList.get(i).getConfirmed()) >1000 && Integer.valueOf(dailList.get(i).getConfirmed()) >5000) {

                rad = 300000;
                rColour = "001CAB";

            }
            else if (Integer.valueOf(dailList.get(i).getConfirmed()) >5000 && Integer.valueOf(dailList.get(i).getConfirmed()) >10000) {

                rad = 400000;
                rColour = "8C2BAB"; //purple

            }

            else if (Integer.valueOf(dailList.get(i).getConfirmed()) >10000 && Integer.valueOf(dailList.get(i).getConfirmed()) >50000)
            {
                rad = 700000;
                rColour= "A9AB47";
            }
            else
            {
                rad = 1000000;
             rColour= "AB1006";
            }*/


        }



    /*    // Create a heat map tile provider, passing it the latlngs of the police stations.
       HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                .weightedData(listWLL)
               .radius(50)
                .build();
        // Add a tile overlay to the map, using the heat map tile provider.
        TileOverlay mOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
*/


        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(MyItem myItem) {

                // Toast.makeText(MainActivity.this, myItem.getPosition().toString(), Toast.LENGTH_LONG).show();

                if (markerShow) {
                    //mk.setVisible(false);
                    mk.remove();
                }

                String co = null, d = null, r = null, n = null, p = null;

                DailyReportsModel model1 = new DailyReportsModel();

                for (DailyReportsModel model : dailList) {
                    if (model.getLongitude().equals(String.valueOf(myItem.getPosition().longitude)) && model.getLatitude().equals(String.valueOf(myItem.getPosition().latitude))) {
                        co = model.getConfirmed();
                        d = model.getDeaths();
                        r = model.getRecovered();
                        n = model.getCountry();
                        p = model.getProvince();

                    }
                }

                mk = map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(MainActivity.this, R.drawable.amu_bubble_shadow, co, d, r, n, p)))
                        .position(myItem.getPosition())
                        .anchor(icon.getAnchorU(), icon.getAnchorV())
                );


                map.animateCamera(CameraUpdateFactory.newLatLng(myItem.getPosition()));


                markerShow = true;

                return true;
            }
        });


        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {


                Toast.makeText(MainActivity.this,"Zoom In to view grouped markers", Toast.LENGTH_LONG).show();
                return true;
            }
        });


        usage.setText("Global Map\\n you can pinch zoom  move around and click on markers to view current stats");

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

            //startActivity(new Intent(getApplicationContext(), MainActivity.class));
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new WorldCovidTracker()).commit();
        } else if (id == R.id.nav_gallery) {

            //startActivity(new Intent(getApplicationContext(), CountryList.class));
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new WorldListView()).commit();

        } else if (id == R.id.nav_slideshow) {

            Intent sendMail = new Intent(Intent.ACTION_SEND);
            sendMail.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            sendMail.setType("plain/text");
            //sendMail.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");
            sendMail.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{"nadargaurav@gmail.com"});
            sendMail.putExtra(Intent.EXTRA_SUBJECT, "Bug Reporting for COVID-19 Tracker Android Application");
            //startActivity(Intent.createChooser(sendMail, "Send Bug Report..."));
            startActivity(sendMail);

        }
        else if(id == R.id.india_list){

           // startActivity(new Intent(getApplicationContext(), IndiaList.class));
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new IndiaCovidTracker()).commit();
        }
        else if(id == R.id.india_listView){
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new IndiaListView()).commit();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public static Bitmap createCustomMarker(Context context, @DrawableRes int resource, String ct, String dt, String rt, String na, String po) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.single_map_marker, null);

        //CircleImageView markerImage = (CircleImageView) marker.findViewById(R.id.user_dp);
        //markerImage.setImageResource(resource);
        TextView c = (TextView) marker.findViewById(R.id.c);
        TextView d = (TextView) marker.findViewById(R.id.d);
        TextView r = (TextView) marker.findViewById(R.id.r);
        TextView n = (TextView) marker.findViewById(R.id.name);
        TextView p = (TextView) marker.findViewById(R.id.province);

        c.setText(ct);
        d.setText(dt);
        r.setText(rt);
        n.setText(na);
        if (po.equals("")) {
            p.setText("All States");
        } else {
            p.setText(po);
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        marker.setLayoutParams(new ViewGroup.LayoutParams(52, ViewGroup.LayoutParams.WRAP_CONTENT));
        marker.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        marker.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(marker.getMeasuredWidth(), marker.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        marker.draw(canvas);

        return bitmap;
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
    }


    public class BackgroundTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, -1);

            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            date2 = df.format(cal.getTime());


            progress.show();
            Log.i("onPre", "level 0");

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            Log.i("omPost", "level 2");
            if(result) {
                editor = prefs.edit();
                editor.putBoolean("firstStart", false);
                editor.commit();
                progress.dismiss();
                startJob();
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new WorldCovidTracker()).commit();
            }

        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Log.i("doing", "level 1");


            try {
                out = openFileOutput("stats.csv", MODE_PRIVATE);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            final OkHttpClient client = new OkHttpClient();
            String url = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

            final Request request = new Request.Builder()
                    .url(url)
                    .build();



            try {
                Response response = client.newCall(request).execute();
                String data = response.body().string();
                out.write(data.getBytes());
                Log.i("task", "write 1");

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    try {
                        out.close();
                        //loadData();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }



            //#################################################################



            OkHttpClient client2 = new OkHttpClient();
            String url2 = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv";

            Request request2 = new Request.Builder()
                    .url(url2)
                    .build();


            try {
                out2 = openFileOutput("deaths.csv", MODE_PRIVATE);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }







            try {
                Response response = client2.newCall(request2).execute();
                String data = response.body().string();
                out2.write(data.getBytes());
                Log.e("error", "not null");

                Log.i("task", "write 2");

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("error", e.getMessage());

            } finally {
                if (out2 != null) {
                    try {
                        out2.close();

                        //getDeaths();
                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                }
                else {

                }
            }


            //#################################################################################




            OkHttpClient client3 = new OkHttpClient();
            String url3 = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_recovered_global.csv";

            Request request3 = new Request.Builder()
                    .url(url3)
                    .build();


            try {
                out3 = openFileOutput("recovered.csv", MODE_PRIVATE);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }







            try {
                Response response = client3.newCall(request3).execute();
                String data = response.body().string();
                out3.write(data.getBytes());

                Log.i("task", "write 3");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out3 != null) {
                    try {
                        out3.close();
                        //getRecovered();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }




            //################################################################################




            try {
                Dailyout = openFileOutput("daily.csv", MODE_PRIVATE);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


            OkHttpClient clientD = new OkHttpClient();
            String urlD = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/"+date2+".csv";

            Request requestD = new Request.Builder()
                    .url(urlD)
                    .build();








            try {
                Response response = clientD.newCall(requestD).execute();
                String data = response.body().string();
                Dailyout.write(data.getBytes());
                Log.i("task", "write 4");

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (out != null) {
                    try {
                        Dailyout.close();

                        //                loadDailyReports();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }




                        return true;
        }

    }



    public boolean isNetworkConnected() {

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true;
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true;
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true;
                }
            }

        } else {

            try {
                NetworkInfo info = cm.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    return true;
                }
            } catch (Exception e) {
                Log.i("status", e.getMessage());

            }
        }

        return false;

    }









    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        appUpdater.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        Boolean signal = isNetworkConnected();
        if(!signal)
        {
            dialog.show();
        }
        else {
            appUpdater.start();
            if(prefs.getBoolean("firstStart", true))
            {
                Log.e("background", "called");
                task = new BackgroundTask();
                task.execute();
            }
        }
    }



}
