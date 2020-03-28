package com.gauravnadar.covid19stats;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.SearchView;
import android.widget.TextView;

import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CountryList extends AppCompatActivity {

    ArrayList<String> countries;
    ArrayList<String> stats;
    ArrayList<String> states;
    int cn;
    ArrayList<CountryListModel> list;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    MyAdapter adapter;

    List<CountryListModel> list2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_list);

        countries = new ArrayList<>();
        stats = new ArrayList<>();
        states = new ArrayList<>();
        list = new ArrayList<>();
        list2 = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.list);


        layoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);



        loadData();




    }


    public void loadData() {

        Log.e("into", "function");


        try {

            // FileInputStream input = new FileInputStream("stats.csv");
            CSVReader reader = new CSVReader(new FileReader("data/data/com.gauravnadar.covid19stats/files/stats.csv"));
            String[] nextLine;

            long count = reader.getLinesRead();
            Log.i("count", String.valueOf(count));
            int a =0;


            while ((nextLine = reader.readNext()) != null) {

                cn = nextLine.length;

                if(a!=0) {

                    countries.add(nextLine[1]);
                    stats.add(nextLine[cn - 1]);
                    states.add(nextLine[0]);
                    list.add(new CountryListModel(nextLine[1], nextLine[0], nextLine[cn-1]));
                    list2.add(new CountryListModel(nextLine[1], nextLine[0], nextLine[cn-1]));


                }
                if (nextLine[1].equals("India")) {
                    Log.i("India", nextLine[1] + ": " + nextLine[cn-1]);
                }

                a++;

            }

            Log.i("length", String.valueOf(countries.size()));
            adapter = new MyAdapter(CountryList.this, list, list);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements Filterable {

        ArrayList<CountryListModel> countryList;
        ArrayList<CountryListModel> fullList;
        Context context;


        public MyAdapter(Context context, ArrayList<CountryListModel> countryList, ArrayList<CountryListModel> fullList) {
            this.countryList = countryList;
            this.context = context;
            this.fullList = new ArrayList<>(countryList);

        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_country_stat_item, null, false);
            MyViewHolder holder = new MyViewHolder(layout);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
            holder.country.setText(countryList.get(position).getCountry());
            holder.stats.setText(countryList.get(position).getConfirmed());
            holder.states.setText(countryList.get(position).getProvince());
            holder.layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(CountryList.this, CountryDetail.class);
                    intent.putExtra("country_name", countryList.get(position).getCountry());
                    intent.putExtra("province_name", countryList.get(position).getProvince());
                    startActivity(intent);

                }
            });

        }

        @Override
        public int getItemCount() {
            return countryList.size();
        }

        @Override
        public Filter getFilter() {

            Log.i("called", "called");

            Filter filterList = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    List<CountryListModel> filter = new ArrayList<>();

                    if(charSequence == null || charSequence.length() ==0)
                    {
                        Log.i("fulllist", "sempty");
                        filter.addAll(fullList);

                    }
                    else
                    {
                        Log.i("fulllist", "not empty");
                        String searchText = charSequence.toString().toLowerCase().trim();
                        Log.i("fulllist", searchText);

                        Log.i("before fulllist", String.valueOf(fullList.size()));
                        for(CountryListModel item: fullList)
                        {
                            Log.i("fulllist", String.valueOf(fullList.size()));

                            if(item.getCountry().toLowerCase().contains(searchText))
                            {
                                filter.add(item);
                            }
                        }

                        Log.i("fulllist", String.valueOf(fullList.size()));
                    }

                    FilterResults results = new FilterResults();
                    results.values = filter;

                    return results;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

                    countryList.clear();
                    countryList.addAll((List)filterResults.values);
                    adapter.notifyDataSetChanged();

                }
            };


            return filterList;
        }








        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView country, stats, states;
            ConstraintLayout layout;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                country = itemView.findViewById(R.id.country);
                stats = itemView.findViewById(R.id.cases);
                states = itemView.findViewById(R.id.state);
                layout = itemView.findViewById(R.id.layout);


            }
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_bar, menu);

        MenuItem searchItem = menu.findItem(R.id.search_menu);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                adapter.getFilter().filter(s);
                return true;
            }
        });

        return true;
    }
}


