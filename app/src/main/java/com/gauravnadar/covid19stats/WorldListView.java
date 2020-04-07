package com.gauravnadar.covid19stats;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gauravnadar.covid19stats.Fragments.CountryDetailView;
import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class WorldListView extends Fragment {

Activity main;
    List<CountryListModel> list;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    MyAdapter adapter;

    List<CountryListModel> list2;

    EditText search;

    public WorldListView() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
View view = inflater.inflate(R.layout.fragment_world_list_view, container, false);

        list = new ArrayList<>();
        list2 = new ArrayList<>();

        recyclerView = (RecyclerView) view.findViewById(R.id.world_list);
        search = (EditText) view.findViewById(R.id.search_world);


        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        new BackgroundTask().execute();


        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                adapter.getFilter().filter(editable);
            }
        });

        return view;
    }



    public class BackgroundTask extends AsyncTask<Void, Integer, Boolean>
    {

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {

                // FileInputStream input = new FileInputStream("stats.csv");
                CSVReader reader = new CSVReader(new FileReader("data/data/com.gauravnadar.covid19stats/files/stats.csv"));
                String[] nextLine;

                long count = reader.getLinesRead();
                Log.i("count", String.valueOf(count));
                int a =0;


                while ((nextLine = reader.readNext()) != null) {

                    int cn = nextLine.length;

                    if(a!=0) {


                        list.add(new CountryListModel(nextLine[1], nextLine[0], nextLine[cn-1]));


                    }
                    if (nextLine[1].equals("India")) {
                        Log.i("India", nextLine[1] + ": " + nextLine[cn-1]);
                    }

                    a++;

                }




            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            adapter = new MyAdapter(getContext(), list);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }



    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements Filterable {

        List<CountryListModel> countryList;
        List<CountryListModel> fullList;
        Context context;
        View layout;


        public MyAdapter(Context context, List<CountryListModel> countryList) {
            this.countryList = countryList;
            this.context = context;
            this.fullList = new ArrayList<>(countryList);

        }

        @NonNull
        @Override
        public MyAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_country_stat_item, null, false);
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

              /*      Intent intent = new Intent(getActivity(), CountryDetail.class);
                    intent.putExtra("country_name", countryList.get(position).getCountry());
                    intent.putExtra("province_name", countryList.get(position).getProvince());
                    startActivity(intent);*/

                    Bundle bundle = new Bundle();
                    bundle.putString("country_name", countryList.get(position).getCountry());
                    bundle.putString("province_name", countryList.get(position).getProvince());

                    CountryDetailView fragment = new CountryDetailView();
                    fragment.setArguments(bundle);

                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                    transaction.replace(R.id.container, fragment).addToBackStack(null).commit();


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

                    if (charSequence == null || charSequence.length() == 0) {
                        Log.i("fulllist", "sempty");
                        filter.addAll(fullList);

                    } else {
                        Log.i("fulllist", "not empty");
                        String searchText = charSequence.toString().toLowerCase().trim();
                        Log.i("fulllist", searchText);

                        Log.i("before fulllist", String.valueOf(fullList.size()));
                        for (CountryListModel item : fullList) {
                            Log.i("fulllist", String.valueOf(fullList.size()));

                            if (item.getCountry().toLowerCase().contains(searchText)) {
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
                    countryList.addAll((List) filterResults.values);
                    adapter.notifyDataSetChanged();

                }
            };


            return filterList;
        }


        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView country, stats, states;
            RelativeLayout layout;

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
    public void onAttach(Context context) {
        super.onAttach(context);

        main = (Activity) context;
    }
}

