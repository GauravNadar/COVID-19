package com.gauravnadar.covid19stats;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.gauravnadar.covid19stats.Modals.MyItem2;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class IndiaListView extends Fragment {


    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    MyAdapter adapter;

    List<MyItem2> list;

    EditText search;

    public IndiaListView() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_india_list_view, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.ind_list);
        search = (EditText) view.findViewById(R.id.search_ind);
        list = new ArrayList<>();

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





    public class BackgroundTask extends AsyncTask<Void, Integer, Boolean>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            try {
                Document doc = Jsoup.connect("https://www.mohfw.gov.in/").get();
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

                    if(a == rows.size() || a == rows.size()-1 || a == rows.size()-2 || a== rows.size()-3) {

                    }
                    else {
                        list.add(new MyItem2(col.get(1).text(), col.get(2).text(), col.get(3).text(), col.get(4).text()));   //state conf reco deat
                    }

                }






            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;


        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            adapter = new MyAdapter(list, getContext());

            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);
        }
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> implements Filterable {

        List<MyItem2> list;
        List<MyItem2> fullList;
        Context context;

        public MyAdapter(List<MyItem2> list, Context context) {
            this.list = list;
            this.context = context;
            this.fullList = new ArrayList<>(list);
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_india_state_stat_item, null, false);
            MyViewHolder holder = new MyViewHolder(layout);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.state.setText(list.get(position).getState());
            holder.confirmed.setText(list.get(position).getConfirmed());
            holder.recovered.setText(list.get(position).getRecovered());
            holder.death.setText(list.get(position).getDeath());
        }

        @Override
        public int getItemCount() {
            return list.size();
        }


        @Override
        public Filter getFilter() {

            Log.i("called", "called");

            Filter filterList = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    List<MyItem2> filter = new ArrayList<>();

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
                        for(MyItem2 item: fullList)
                        {
                            Log.i("fulllist", String.valueOf(fullList.size()));

                            if(item.getState().toLowerCase().contains(searchText))
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

                    list.clear();
                    list.addAll((List)filterResults.values);
                    adapter.notifyDataSetChanged();

                }
            };


            return filterList;
        }






        public class MyViewHolder extends RecyclerView.ViewHolder{

            TextView state, confirmed, recovered, death;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);

                state = (TextView) itemView.findViewById(R.id.ind_state);
                confirmed = (TextView) itemView.findViewById(R.id.ind_c);
                recovered = (TextView) itemView.findViewById(R.id.ind_r);
                death = (TextView) itemView.findViewById(R.id.ind_d);
            }
        }
    }

}




