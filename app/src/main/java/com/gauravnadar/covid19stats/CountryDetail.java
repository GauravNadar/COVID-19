package com.gauravnadar.covid19stats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.opencsv.CSVReader;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CountryDetail extends AppCompatActivity implements OnMapReadyCallback {

    String country, province;
    TextView name, c_case, d_case, r_case;
    BarChart chart;

    FileOutputStream out = null;
    FileOutputStream out2 = null;

    ArrayList<Map<String, String>> list;

    ArrayList<String> confirmed;
    ArrayList<String> deaths;
    ArrayList<String> recovered;

    String third, second, last;
    String d_third, d_second, d_last;
String day3, day2, day1;
String r_third, r_second, r_last;
    String[] countries;

    MapView mapView;
GoogleMap map;
    private static final String MAP_VIEW_BUNDLE_KEY = "MapViewBundleKey";

    List<Model> coords;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    List<NewsModel.ArticlesBean> newsList;
    NewsAdapter adapter;

    String ISO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_detail);

        country = getIntent().getStringExtra("country_name");
        province = getIntent().getStringExtra("province_name");

        name = findViewById(R.id.c_name);
        c_case = findViewById(R.id.c_cases);
        d_case = findViewById(R.id.d_cases);
        r_case = findViewById(R.id.r_cases);
        chart = (BarChart) findViewById(R.id.chart1);
        mapView = (MapView) findViewById(R.id.mapView);
        recyclerView = (RecyclerView) findViewById(R.id.news_recycler);


        Bundle mapViewBundle = null;
        if(savedInstanceState != null)
        {
            mapViewBundle = savedInstanceState.getBundle(MAP_VIEW_BUNDLE_KEY);
        }


        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);



        Map<String, String> countrieNames = new HashMap<>();
        for (String iso: Locale.getISOCountries()){

            Locale l = new Locale("", iso);
            countrieNames.put(l.getDisplayCountry(), iso);
        }


        String CapIso = countrieNames.get(country);
        if(CapIso == null)
        {
         ISO = "";
        }
        else {
            ISO = CapIso.toLowerCase();
        }

        CapIso = countrieNames.get(country);
        if(CapIso == null)
        {
            Log.d("locale", "empty");
        }
        else {
            Log.d("locale", countrieNames.get(country));
        }



list = new ArrayList<>();
newsList = new ArrayList<>();


        layoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        //recyclerView.hasFixedSize();
        recyclerView.setHasFixedSize(true);


confirmed = new ArrayList<>();
        deaths = new ArrayList<>();
        recovered = new ArrayList<>();

        coords = new ArrayList<>();

        name.setText(country);




//getDailyReports();

        countries = new String[3];


        getConfirmed();

        getDeaths();

        getRecovered();

        getNews(country);


ArrayList<BarEntry> barEntries = new ArrayList<>();

    barEntries.add(new BarEntry(0, Float.valueOf(third)));
        barEntries.add(new BarEntry(1, Float.valueOf(second)));
        barEntries.add(new BarEntry(2, Float.valueOf(last)));

        BarDataSet barDataSet = new BarDataSet(barEntries, "Confirmed");
        barDataSet.setColor(ColorTemplate.COLORFUL_COLORS[0]);







        countries = new String[] {day3, day2, day1, "sample", "sample2"};


getCoordinates(country, province);

plotMap();


        ArrayList<BarEntry> barEntries2 = new ArrayList<>();

        barEntries2.add(new BarEntry(1, Float.valueOf(d_third)));
        barEntries2.add(new BarEntry(2, Float.valueOf(d_second)));
        barEntries2.add(new BarEntry(3, Float.valueOf(d_last)));

        BarDataSet barDataSet2 = new BarDataSet(barEntries2, "Deaths");
        barDataSet2.setColor(ColorTemplate.COLORFUL_COLORS[1]);





        ArrayList<BarEntry> barEntries3 = new ArrayList<>();

        barEntries3.add(new BarEntry(1, Float.valueOf(r_third)));
        barEntries3.add(new BarEntry(2, Float.valueOf(r_second)));
        barEntries3.add(new BarEntry(3, Float.valueOf(r_last)));

        BarDataSet barDataSet3 = new BarDataSet(barEntries3, "Recovered");
        barDataSet3.setColor(ColorTemplate.COLORFUL_COLORS[2]);



        BarData barData = new BarData(barDataSet, barDataSet2);

        chart.setData(barData);

        Float groupSpace = 0.1f;
        Float barSpace = 0.02f;
        float barWidth = 0.30f;

        barData.setBarWidth(barWidth);

        chart.groupBars(0, groupSpace, barSpace);

        XAxis xAxis = chart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return countries[(int)value];
            }
        });
        xAxis.setPosition(XAxis.XAxisPosition.TOP);


    }

    private void getNews(String country) {

        GetDataServices helper = RetrofitClientInstance.getRetrofitInstance().create(GetDataServices.class);
        Call<NewsModel> call = helper.getTopHeadlines(ISO, "covid", "54a88ba0d81d4a9081ac7b60b546237b");

        call.enqueue(new Callback<NewsModel>() {
            @Override
            public void onResponse(Call<NewsModel> call, Response<NewsModel> response) {

                Log.d("code", String.valueOf(response.code()));
                Log.d("data", response.body().toString());
                List<NewsModel.ArticlesBean> list = new ArrayList<>();
                list = response.body().getArticles();

               // Log.d("content", list.get(0).getTitle());

                newsList = response.body().getArticles();
                Log.d("length", String.valueOf(newsList.size()));
                adapter = new NewsAdapter(CountryDetail.this, newsList);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onFailure(Call<NewsModel> call, Throwable t) {

                Log.d("error", t.getMessage());
            }
        });

    }


    private void getCoordinates(String country, String province) {


        try {

            // FileInputStream input = new FileInputStream("stats.csv");
            CSVReader reader = new CSVReader(new FileReader("data/data/com.gauravnadar.covid19stats/files/stats.csv"));
            String[] nextLine;


            while ((nextLine = reader.readNext()) != null) {

                int cn = nextLine.length;

                if (nextLine[1].equals(country)) {


                    //2 3
                    LatLng latLng = new LatLng(Double.valueOf(nextLine[2]), Double.valueOf(nextLine[3]));
                    Model model = new Model(nextLine[1], nextLine[0], nextLine[2], nextLine[3]);
                    coords.add(model);

                }

            }


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
        map = googleMap;

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.valueOf(coords.get(0).getLatitude()), Double.valueOf(coords.get(0).getLongitude())), 14));

    }


    private void plotMap() {






        for(int a=0; a<coords.size(); a++)
        {



        }

    }


    private void getConfirmed() {


        try {

            // FileInputStream input = new FileInputStream("stats.csv");
            CSVReader reader = new CSVReader(new FileReader("data/data/com.gauravnadar.covid19stats/files/stats.csv"));
            String[] nextLine;


            while ((nextLine = reader.readNext()) != null) {

                int cn = nextLine.length;


                if(nextLine[1].equals("Country/Region"))
                {
                    day3 = nextLine[cn-3];
                    day2 = nextLine[cn-2];
                    day1 = nextLine[cn-1];

                }

                if (nextLine[1].equals(country) && nextLine[0].equals(province)) {
                    Log.i("India", nextLine[3] + ": " + nextLine[cn-1]);

                    c_case.setText(nextLine[cn-1]);


                    last = nextLine[cn-1];
                    second = nextLine[cn-2];
                    third = nextLine[cn-3];


                }



            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getDeaths() {


        try {

            // FileInputStream input = new FileInputStream("stats.csv");
            CSVReader reader = new CSVReader(new FileReader("data/data/com.gauravnadar.covid19stats/files/deaths.csv"));
            String[] nextLine;


            while ((nextLine = reader.readNext()) != null) {

                int cn = nextLine.length;


                if (nextLine[1].equals(country) && nextLine[0].equals(province)) {
                    // Log.i("India", nextLine[1] + ": " + nextLine[cn-1]);

                    d_case.setText(nextLine[cn-1]);

                    d_last = nextLine[cn-1];
                    d_second = nextLine[cn-2];
                    d_third = nextLine[cn-3];
                }

            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadData() {



    }

    private void getRecovered() {


        try {

            // FileInputStream input = new FileInputStream("stats.csv");
            CSVReader reader2 = new CSVReader(new FileReader("data/data/com.gauravnadar.covid19stats/files/recovered.csv"));
            String[] nextLine;


            while ((nextLine = reader2.readNext()) != null) {

                int cn = nextLine.length;


                if (nextLine[1].equals(country) && nextLine[0].equals(province)) {
                    // Log.i("India", nextLine[1] + ": " + nextLine[cn-1]);

                    r_case.setText("");


                    r_third = nextLine[cn-3];
                    r_second = nextLine[cn-2];
                    r_last = nextLine[cn-1];
                }

            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void getDailyReports()
    {


        try {

            // FileInputStream input = new FileInputStream("stats.csv");
            CSVReader reader3 = new CSVReader(new FileReader("data/data/com.gauravnadar.covid19stats/files/daily.csv"));
            String[] nextLine;


            while ((nextLine = reader3.readNext()) != null) {

                int cn = nextLine.length;

                //DailyReportsModel data = new DailyReportsModel(nextLine[0], nextLine[1], nextLine[2], nextLine[3], nextLine[4], nextLine[5], nextLine[6], nextLine[7] , nextLine[8] , nextLine[9] , nextLine[10] , nextLine[11]);

            }

            return;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    public class MyXAxisValueFormatter extends ValueFormatter{

        String[] Mvalues = new String[]{};



        public MyXAxisValueFormatter(String[] values) {
            this.Mvalues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return Mvalues[(int)value];
        }
    }



    public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder>{

        Context context;
        List<NewsModel.ArticlesBean> newsList;


        public NewsAdapter(Context context, List<NewsModel.ArticlesBean> newsList) {
            this.context = context;
            this.newsList = newsList;
        }

        @NonNull
        @Override
        public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_news_item, null, false);
            NewsViewHolder holder = new NewsViewHolder(layout);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {

            holder.tit.setText(newsList.get(position).getTitle());
            holder.tit.setSelected(true);
            holder.desc.setText(newsList.get(position).getDescription());
            holder.pub.setText(newsList.get(position).getPublishedAt());

            Picasso.Builder builder = new Picasso.Builder(getApplicationContext());
            builder.downloader(new OkHttp3Downloader(getApplicationContext()));
            builder.build().load(newsList.get(position).getUrlToImage())//model.getLogo_url())
                    // .placeholder((R.drawable.ic_launcher_background))
                    //.error(R.drawable.ic_launcher_background)
                    .into(holder.pic);

        }

        @Override
        public int getItemCount() {
            return newsList.size();
        }


        public class NewsViewHolder extends RecyclerView.ViewHolder{

            ImageView pic;
            TextView tit, desc, pub;

            public NewsViewHolder(@NonNull View itemView) {
                super(itemView);

                pic = itemView.findViewById(R.id.n_image);
                tit = itemView.findViewById(R.id.n_title);
                desc = itemView.findViewById(R.id.n_desc);
                pub = itemView.findViewById(R.id.n_pub);

            }
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