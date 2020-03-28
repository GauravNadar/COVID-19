package com.gauravnadar.covid19stats;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.AsyncTask;
import android.os.Bundle;

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
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
import com.google.maps.android.heatmaps.WeightedLatLng;
import com.google.maps.android.ui.IconGenerator;
import com.opencsv.CSVReader;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {

    FileInputStream in = null;
    FileOutputStream out = null;
    FileOutputStream out2 = null;
    FileOutputStream out3 = null;

    FileInputStream Dailyin = null;
    FileOutputStream Dailyout = null;
    ArrayList<DailyReportsModel> dailList;



    BottomSheetBehavior bottomSheetBehavior;


    GoogleMap map;
    MapView mapView;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

ProgressDialog progress;

Boolean markerShow = false;
    Marker mk = null;

    Boolean MapSet = false;
    TextView usage;

    List<LatLng> listLL;
    List<WeightedLatLng> listWLL;

    ClusterManager<MyItem> mClusterManager;

    AppUpdater appUpdater;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //SharedPreferences preferences = getSharedPreferences("prefs", MODE_PRIVATE);
        //boolean firstStart = preferences.getBoolean("firststart", true);


        appUpdater = new AppUpdater(this)
                .setGitHubUserAndRepo("GauravNadar", "COVID-19")
                .setDisplay(Display.DIALOG)
                    .setUpdateFrom(UpdateFrom.XML)
                .setUpdateXML("https://raw.githubusercontent.com/GauravNadar/COVID-19/master/app/update.xml");
        appUpdater.start();



        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        usage = (TextView) findViewById(R.id.usage);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();*/

               if(!MapSet)
                {
                    getDailyReports();
                    loadMap();
                    usage.setVisibility(View.VISIBLE);
                    MapSet = true;
                }
                else{

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

        progress = new ProgressDialog(MainActivity.this);
        progress.setMessage("This may take up few seconds depending on your Internet speed");
        progress.setTitle("Loading Global Data");
        progress.setCanceledOnTouchOutside(false);







        startJob();














       // View bottomsheet = findViewById(R.id.nested);

        mapView = findViewById(R.id.map);
       // bottomSheetBehavior = BottomSheetBehavior.from(bottomsheet);







        dailList = new ArrayList<>();

        listLL = new ArrayList<>();
        listWLL = new ArrayList<>();



        Bundle mapViewBundle = null;
        if(savedInstanceState != null)
        {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }


        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);

        getDailyReports();



        try {
            in = openFileInput("stats.csv");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            out = openFileOutput("stats.csv", MODE_PRIVATE);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }



        // getData();

     //######################################################################################


    }

    private void startJob() {


        ComponentName componentName = new ComponentName(this, Scheduler.class);
        JobInfo info = new JobInfo.Builder(111, componentName)
                .setPeriodic(15*60*1000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();


        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resutCode = scheduler.schedule(info);


        if(resutCode == JobScheduler.RESULT_SUCCESS)
        {
            Log.d("Job", "Result Success");

        }
        else
        {
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
                if(nextLine[3].equals("Country_Region"))
                {}
                else
                {
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
        if(mapViewBundle == null){
            mapViewBundle = new Bundle();
            outState.putBundle(MAP_VIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }





    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.e("timing","onMapReady");
map = googleMap;



        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if(markerShow) {
                    mk.remove();
                }

            }
        });

        final IconGenerator icon = new IconGenerator(this);

        map.setOnCircleClickListener(new GoogleMap.OnCircleClickListener() {
    @Override
    public void onCircleClick(Circle circle) {


        if(markerShow)
        {
            //mk.setVisible(false);
            mk.remove();
        }

        String co = null, d = null, r= null, n=null, p=null;

        DailyReportsModel model1= new DailyReportsModel();

        for (DailyReportsModel model : dailList)
        {
            if(model.getLongitude().equals(String.valueOf(circle.getCenter().longitude))  && model.getLatitude().equals(String.valueOf(circle.getCenter().latitude)))
            {
                co = model.getConfirmed();
                 d = model.getDeaths();
                r = model.getRecovered();
                n = model.getCountry();
                p = model.getProvince();

            }
        }

        mk = map.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(MainActivity.this, R.drawable.amu_bubble_shadow, co , d , r , n, p)))
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


    public void loadMap(){

        Log.e("timing","loadMap");

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

        for (int i=1; i<dailList.size(); i++)
        {


               // latLng = new LatLng(Double.valueOf(dailList.get(i).getLatitude()),  Double.valueOf(dailList.get(i).getLongitude()));
               /* Circle c = map.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(fixed)
                        //.getStrokeWidth()
                        .fillColor(0x88AB1006)  //fillColor(0x220000FF)
                        .clickable(true)
                        .strokeWidth(2.0f)

                );*/


    MyItem offsetItem = new MyItem(Double.valueOf(dailList.get(i).getLatitude()), Double.valueOf(dailList.get(i).getLongitude()));


    mClusterManager.addItem(offsetItem);




            LatLng latLng2 = new LatLng(Double.valueOf(dailList.get(i).getLatitude()),  Double.valueOf(dailList.get(i).getLongitude()));
            WeightedLatLng latLng3 = new WeightedLatLng(latLng2, 500);
            int rad = 0;




            listLL.add(latLng2);
            listWLL.add(latLng3);

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

                if(markerShow)
                {
                    //mk.setVisible(false);
                    mk.remove();
                }

                String co = null, d = null, r= null, n=null, p=null;

                DailyReportsModel model1= new DailyReportsModel();

                for (DailyReportsModel model : dailList)
                {
                    if(model.getLongitude().equals(String.valueOf(myItem.getPosition().longitude))  && model.getLatitude().equals(String.valueOf(myItem.getPosition().latitude)))
                    {
                        co = model.getConfirmed();
                        d = model.getDeaths();
                        r = model.getRecovered();
                        n = model.getCountry();
                        p = model.getProvince();

                    }
                }

                mk = map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(MainActivity.this, R.drawable.amu_bubble_shadow, co , d , r , n, p)))
                        .position(myItem.getPosition())
                        .anchor(icon.getAnchorU(), icon.getAnchorV())
                );


                map.moveCamera(CameraUpdateFactory.newLatLng(myItem.getPosition()));

                markerShow = true;

                return true;
            }
        });


        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {


                Toast.makeText(MainActivity.this, String.valueOf(cluster.getSize()), Toast.LENGTH_LONG).show();
                return true;
            }
        });


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
        getMenuInflater().inflate(R.menu.main, menu);
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

        } else if (id == R.id.nav_gallery) {

            startActivity(new Intent(MainActivity.this, CountryList.class));

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_tools) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    public static Bitmap createCustomMarker(Context context, @DrawableRes int resource, String ct, String dt, String rt, String na, String po) {

        View marker = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.single_map_marker, null);

        //CircleImageView markerImage = (CircleImageView) marker.findViewById(R.id.user_dp);
        //markerImage.setImageResource(resource);
        TextView c = (TextView)marker.findViewById(R.id.c);
        TextView d = (TextView)marker.findViewById(R.id.d);
        TextView r = (TextView)marker.findViewById(R.id.r);
        TextView n = (TextView)marker.findViewById(R.id.name);
        TextView p = (TextView)marker.findViewById(R.id.province);

        c.setText(ct);
        d.setText(dt);
        r.setText(rt);
        n.setText(na);
        if(po.equals(""))
        {
            p.setText("All States");
        }
        else {
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


    public class BackgroundTask extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Log.i("onPre", "0");

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.i("omPost", "2");
            progress.dismiss();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.i("doing", "1");
            startJob();
            return null;
        }
    }


    public static class Worker2 extends Thread {

        private static final AtomicBoolean alive = new AtomicBoolean(true);
        private ConcurrentLinkedQueue taskQueue = new ConcurrentLinkedQueue();
        private ProgressDialog progress;
        Context context;

        public Worker2(Context context)
        {
            super("Worker");
            this.context = context;

            start();
            progress = new ProgressDialog(context);
            progress.setMessage("This may take up few seconds depending on your Internet speed");
            progress.setTitle("Loading Global Data");
            progress.setCanceledOnTouchOutside(false);
            //progress.show();
        }

        @Override
        public void run() {

            while (alive.get()) {
                Runnable task = (Runnable) taskQueue.poll();
                if(task!= null)
                {
                    task.run();
       // progress.show();

                }
            }

            Log.i("Terminated", "xx");
            progress.dismiss();
        }


        public Worker2 execute(Runnable task){

            taskQueue.add(task);
            return this;
        }

        public void quit()
        {
            alive.set(false);
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
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
    }



}
