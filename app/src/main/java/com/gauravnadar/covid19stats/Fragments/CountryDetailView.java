package com.gauravnadar.covid19stats.Fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.PersistableBundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.gauravnadar.covid19stats.CountryDetail;
import com.gauravnadar.covid19stats.GetDataServices;
import com.gauravnadar.covid19stats.Modals.Model;
import com.gauravnadar.covid19stats.NewsModel;
import com.gauravnadar.covid19stats.R;
import com.gauravnadar.covid19stats.RetrofitClientInstance;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class CountryDetailView extends Fragment implements OnMapReadyCallback {



    String country, province;
    TextView name, c_case, d_case, r_case, provi, news_banner;
    BarChart chart;

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
    Activity main;

    public CountryDetailView() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_country_detail_view, container, false);



                country = getArguments().getString("country_name");
        province = getArguments().getString("province_name");

        name = view.findViewById(R.id.c_name);
        provi = view.findViewById(R.id.provi);
        c_case = view.findViewById(R.id.c_cases);
        d_case = view.findViewById(R.id.d_cases);
        r_case = view.findViewById(R.id.r_cases);
        chart = (BarChart) view.findViewById(R.id.chart1);
        mapView = (MapView) view.findViewById(R.id.mapView);
        recyclerView = (RecyclerView) view.findViewById(R.id.news_recycler);
        news_banner = (TextView) view.findViewById(R.id.news_heading);


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


        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        //recyclerView.hasFixedSize();
        recyclerView.setHasFixedSize(true);


        confirmed = new ArrayList<>();
        deaths = new ArrayList<>();
        recovered = new ArrayList<>();

        coords = new ArrayList<>();

        name.setText(country);
        provi.setText(province);




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


        View focus = main.getCurrentFocus();
        if(focus!=null)
        {

            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
        }

        return view;
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
                if(response.body().getArticles().isEmpty())
                {
                    news_banner.setVisibility(View.INVISIBLE);
                }
                else
                {
                    news_banner.setVisibility(View.VISIBLE);
                }

                newsList = response.body().getArticles();
                Log.d("length", String.valueOf(newsList.size()));
                adapter = new NewsAdapter(getContext(), newsList);
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
        public NewsAdapter.NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_news_item, null, false);
            NewsAdapter.NewsViewHolder holder = new NewsAdapter.NewsViewHolder(layout);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull NewsAdapter.NewsViewHolder holder, int position) {

            holder.tit.setText(newsList.get(position).getTitle());
            holder.tit.setSelected(true);
            holder.desc.setText(newsList.get(position).getDescription());
            holder.pub.setText(newsList.get(position).getPublishedAt());

            Picasso.Builder builder = new Picasso.Builder(getContext());
            builder.downloader(new OkHttp3Downloader(getContext()));
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
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        main = (Activity) context;
    }
}
