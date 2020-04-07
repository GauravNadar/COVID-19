package com.gauravnadar.covid19stats;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class IndiaList extends AppCompatActivity {

    private static String TAG = "IndiaList";
    List<IndiaListModel> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_india_list);

        list = new ArrayList<>();

NetworkBackground background = new NetworkBackground();
background.execute();


    }

    public class NetworkBackground extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {


            try {
                Document doc = Jsoup.connect("https://www.mohfw.gov.in/").get();
                Log.i(TAG, doc.title());
               // Elements elements = doc.getElementsByClass("data-table table-responsive");
                Elements elements = doc.getElementsByClass("data-table table-responsive");
                elements.html();



            Element table = doc.select("tbody").get(0);
            Elements rows = table.select("tr");

            for(int a=0; a<rows.size(); a++){

                Element row = rows.get(a);
                Elements col = row.select("td");

                if(a == rows.size()-1) {

                }
                else {
                    list.add(new IndiaListModel(col.get(1).text(), col.get(2).text(), col.get(3).text(), col.get(4).text()));
                }

            }

                Log.i(TAG, String.valueOf(list.size()));


            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;

        }
    }
}
